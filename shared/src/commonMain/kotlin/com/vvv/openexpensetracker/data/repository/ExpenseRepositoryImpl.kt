package com.vvv.openexpensetracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.data.source.remote.GoogleDriveApi
import com.vvv.openexpensetracker.db.AppDatabase
import com.vvv.openexpensetracker.db.ExpenseEntity
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExpenseRepositoryImpl(
    private val database: AppDatabase,
    private val localStorage: LocalStorage,
    private val googleDriveApi: GoogleDriveApi,
    private val googleAuthRepository: GoogleAuthRepository
) : ExpenseRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val queries = database.appDatabaseQueries
    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    init {
        migrateFromFileToDb()
    }

    private fun migrateFromFileToDb() {
        try {
            val content = localStorage.loadExpensesFile()
            if (!content.isNullOrEmpty()) {
                val list = json.decodeFromString<List<Expense>>(content)
                saveLocalExpenses(list)
                // Once migrated, we can "clear" the file content or just leave it.
                // For safety, I'll clear it so we don't migrate again.
                localStorage.saveExpensesFile("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getExpenses(): Flow<List<Expense>> {
        return queries.getExpenses()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { it.toExpense() }
            }
    }

    override suspend fun saveExpense(expense: Expense) {
        queries.insertExpense(
            id = expense.id,
            description = expense.description,
            amount = expense.amount,
            date = expense.date,
            category = expense.category,
            lastModified = expense.lastModified
        )
        triggerAutoSync()
    }

    override suspend fun deleteExpense(id: String) {
        queries.deleteExpense(id)
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
            val localList = getExpenses().first()
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
                    localStorage.saveString("last_sync_time", Clock.System.now().toEpochMilliseconds().toString())
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

    private fun saveLocalExpenses(expenses: List<Expense>) {
        database.transaction {
            // We could optimize this by only inserting what changed, 
            // but for simplicity and with INSERT OR REPLACE it's fine for now.
            // However, we should handle deletions if remote has fewer items?
            // Actually mergeExpenses handles conflict resolution.
            // If remote deleted something, it won't be in mergedList.
            // So we should probably clear and re-insert or do a proper diff.
            queries.deleteAllExpenses()
            expenses.forEach { expense ->
                queries.insertExpense(
                    id = expense.id,
                    description = expense.description,
                    amount = expense.amount,
                    date = expense.date,
                    category = expense.category,
                    lastModified = expense.lastModified
                )
            }
        }
    }

    override fun getLastSyncTime(): Long {
        val timeStr = localStorage.getString("last_sync_time")
        return timeStr?.toLongOrNull() ?: 0L
    }

    private fun mergeExpenses(local: List<Expense>, remote: List<Expense>): List<Expense> {
        val allExpenses = (local + remote).groupBy { it.id }
        return allExpenses.map { (_, list) ->
            list.maxByOrNull { it.lastModified } ?: list.first()
        }
    }

    private fun ExpenseEntity.toExpense(): Expense {
        return Expense(
            id = id,
            description = description,
            amount = amount,
            date = date,
            category = category,
            lastModified = lastModified
        )
    }
}
