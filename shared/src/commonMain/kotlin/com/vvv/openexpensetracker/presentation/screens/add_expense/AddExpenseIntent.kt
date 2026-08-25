package com.vvv.openexpensetracker.presentation.screens.add_expense

import com.vvv.openexpensetracker.domain.util.ParsedReceipt

sealed interface AddExpenseIntent {
    data class LoadExpense(val id: String?) : AddExpenseIntent
    data class DescriptionChanged(val text: String) : AddExpenseIntent
    data class AmountChanged(val text: String) : AddExpenseIntent
    data class DateChanged(val timestamp: Long) : AddExpenseIntent
    data class CategoryChanged(val category: String) : AddExpenseIntent
    data class ReceiptScanned(val receipt: ParsedReceipt) : AddExpenseIntent
    data object SaveExpense : AddExpenseIntent
    data object ResetSaveState : AddExpenseIntent
}
