package com.vvv.openexpensetracker.domain.repository

import com.vvv.openexpensetracker.domain.model.Expense

interface GoogleDriveRepository {
    suspend fun findExpensesFile(): String?
    suspend fun downloadExpenses(fileId: String): List<Expense>?
    suspend fun createExpensesFile(): String?
    suspend fun updateExpensesFile(fileId: String, expenses: List<Expense>): Boolean
    fun clearCache()
}
