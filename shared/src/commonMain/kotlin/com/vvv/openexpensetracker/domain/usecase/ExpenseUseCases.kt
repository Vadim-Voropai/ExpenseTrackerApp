package com.vvv.openexpensetracker.domain.usecase

import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<Expense>> = repository.getExpenses()
}

class SaveExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense) = repository.saveExpense(expense)
}

class DeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteExpense(id)
}

class UndoDeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String) = repository.undoDeleteExpense(id)
}

class SyncExpensesUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.syncWithGoogleDrive()
}

class GetLastSyncTimeUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Long = repository.getLastSyncTime()
}
