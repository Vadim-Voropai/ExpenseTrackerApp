package com.vvv.openexpensetracker.domain.repository

import com.vvv.openexpensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(): Flow<List<Expense>>
    suspend fun saveExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
    suspend fun syncWithGoogleDrive(): Result<Unit>
    fun getLastSyncTime(): Long
}
