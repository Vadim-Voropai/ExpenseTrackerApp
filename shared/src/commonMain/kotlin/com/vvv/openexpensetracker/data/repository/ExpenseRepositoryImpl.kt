package com.vvv.openexpensetracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.vvv.openexpensetracker.core.Constants
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.db.AppDatabase
import com.vvv.openexpensetracker.db.ExpenseEntity
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.GoogleDriveRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ExpenseRepositoryImpl(
    private val database: AppDatabase,
    private val localStorage: LocalStorage,
    private val googleDriveRepository: GoogleDriveRepository,
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
        if (googleAuthRepository.isSignedIn()) {
            repositoryScope.launch {
                syncWithGoogleDrive()
            }
        }
    }

    override suspend fun syncWithGoogleDrive(): Result<Unit> {
        if (!googleAuthRepository.isSignedIn()) {
            return Result.failure(Exception("Google Sign-In is required to sync"))
        }

        return try {
                val localList = getExpenses().first()
            var fileId = googleDriveRepository.findExpensesFile()

            val mergedList = if (fileId != null) {
                val remoteContent = googleDriveRepository.downloadExpensesFile(fileId)
                if (!remoteContent.isNullOrEmpty()) {
                    val remoteList = json.decodeFromString<List<Expense>>(remoteContent)
                    mergeExpenses(localList, remoteList)
                } else {
                    localList
                }
            } else {
                fileId = googleDriveRepository.createExpensesFile()
                localList
            }

            if (fileId != null) {
                val uploadContent = json.encodeToString(mergedList)
                val success = googleDriveRepository.updateExpensesFile(fileId, uploadContent)
                if (success) {
                    saveLocalExpenses(mergedList)
                    localStorage.saveString(Constants.KEY_LAST_SYNC_TIME, Clock.System.now().toEpochMilliseconds().toString())
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
        val timeStr = localStorage.getString(Constants.KEY_LAST_SYNC_TIME)
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
