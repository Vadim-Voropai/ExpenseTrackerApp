package com.vadim.expensetracker.data.repository

import com.vadim.expensetracker.data.source.local.LocalStorage
import com.vadim.expensetracker.data.source.remote.GoogleDriveApi
import com.vadim.expensetracker.domain.model.Expense
import com.vadim.expensetracker.domain.repository.ExpenseRepository
import com.vadim.expensetracker.domain.repository.GoogleAuthRepository
import kotlinx.datetime.Clock.System
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExpenseRepositoryImpl(
    private val localStorage: LocalStorage,
    private val googleDriveApi: GoogleDriveApi,
    private val googleAuthRepository: GoogleAuthRepository
) : ExpenseRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _expensesFlow = MutableStateFlow<List<Expense>>(emptyList())
    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    init {
        loadLocalExpenses()
    }

    private fun loadLocalExpenses() {
        try {
            val content = localStorage.loadExpensesFile()
            if (!content.isNullOrEmpty()) {
                val list = json.decodeFromString<List<Expense>>(content)
                _expensesFlow.value = list.sortedByDescending { it.date }
            } else {
                _expensesFlow.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _expensesFlow.value = emptyList()
        }
    }

    private fun saveLocalExpenses(list: List<Expense>) {
        try {
            val content = json.encodeToString(list)
            localStorage.saveExpensesFile(content)
            _expensesFlow.value = list.sortedByDescending { it.date }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getExpenses(): Flow<List<Expense>> {
        return _expensesFlow.asStateFlow()
    }

    override suspend fun saveExpense(expense: Expense) {
        val currentList = _expensesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == expense.id }
        if (index >= 0) {
            currentList[index] = expense
        } else {
            currentList.add(expense)
        }
        saveLocalExpenses(currentList)
        triggerAutoSync()
    }

    override suspend fun deleteExpense(id: String) {
        val currentList = _expensesFlow.value.filter { it.id != id }
        saveLocalExpenses(currentList)
        triggerAutoSync()
    }

    private fun triggerAutoSync() {
        val token = googleAuthRepository.accessToken.value
        if (token != null) {
            repositoryScope.launch {
                syncWithGoogleDrive()
            }
        }
    }

    override suspend fun syncWithGoogleDrive(): Result<Unit> {
        val token = googleAuthRepository.accessToken.value
            ?: return Result.failure(Exception("Google Sign-In is required to sync"))

        return try {
            val localList = _expensesFlow.value
            var fileId = googleDriveApi.findExpensesFile(token)

            val mergedList = if (fileId != null) {
                // File exists, download and merge
                val remoteContent = googleDriveApi.downloadExpensesFile(token, fileId)
                if (!remoteContent.isNullOrEmpty()) {
                    val remoteList = json.decodeFromString<List<Expense>>(remoteContent)
                    mergeExpenses(localList, remoteList)
                } else {
                    localList
                }
            } else {
                // File does not exist, create it
                fileId = googleDriveApi.createExpensesFile(token)
                localList
            }

            if (fileId != null) {
                val uploadContent = json.encodeToString(mergedList)
                val success = googleDriveApi.updateExpensesFile(token, fileId, uploadContent)
                if (success) {
                    saveLocalExpenses(mergedList)
                    localStorage.saveString("last_sync_time", System.now().toEpochMilliseconds().toString())
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to upload expenses to Google Drive"))
                }
            } else {
                Result.failure(Exception("Failed to create expenses file on Google Drive"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getLastSyncTime(): Long {
        val timeStr = localStorage.getString("last_sync_time")
        return timeStr?.toLongOrNull() ?: 0L
    }

    // Merges local and remote lists. Resolves conflicts using lastModified timestamp (latest wins)
    private fun mergeExpenses(local: List<Expense>, remote: List<Expense>): List<Expense> {
        val allExpenses = (local + remote).groupBy { it.id }
        return allExpenses.map { (_, list) ->
            list.maxByOrNull { it.lastModified } ?: list.first()
        }
    }
}
