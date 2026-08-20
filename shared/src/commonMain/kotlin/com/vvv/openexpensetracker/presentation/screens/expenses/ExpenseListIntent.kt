package com.vvv.openexpensetracker.presentation.screens.expenses

sealed interface ExpenseListIntent {
    data class SearchQueryChanged(val query: String) : ExpenseListIntent
    data class CategoryFilterChanged(val category: String?) : ExpenseListIntent
    data class DeleteExpense(val id: String) : ExpenseListIntent
    data class UndoDelete(val id: String) : ExpenseListIntent
    data object SyncExpenses : ExpenseListIntent
}
