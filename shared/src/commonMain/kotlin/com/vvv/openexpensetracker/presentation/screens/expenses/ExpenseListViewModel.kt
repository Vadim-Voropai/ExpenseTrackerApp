package com.vvv.openexpensetracker.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.usecase.DeleteExpenseUseCase
import com.vvv.openexpensetracker.domain.usecase.GetCurrencyUseCase
import com.vvv.openexpensetracker.domain.usecase.GetExpensesUseCase
import com.vvv.openexpensetracker.domain.usecase.SyncExpensesUseCase
import com.vvv.openexpensetracker.domain.usecase.UndoDeleteExpenseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ExpenseListUiEffect {
    data class ShowUndoSnackbar(val expenseId: String) : ExpenseListUiEffect
    data class ShowError(val message: String) : ExpenseListUiEffect
}

data class ExpenseListUIState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val currency: AppCurrency = AppCurrency.USD,
    val isRefreshing: Boolean = false,
    val expenses: List<Expense> = emptyList()
)

class ExpenseListViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val undoDeleteExpenseUseCase: UndoDeleteExpenseUseCase,
    private val syncExpensesUseCase: SyncExpensesUseCase,
    getCurrencyUseCase: GetCurrencyUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)

    private val _effect = Channel<ExpenseListUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<ExpenseListUIState> = combine(
        getExpensesUseCase(),
        combine(
            _searchQuery,
            _selectedCategory,
            getCurrencyUseCase.currency,
            _isRefreshing
        ) { query, category, currency, refreshing ->
            FourArgs(query, category, currency, refreshing)
        }
    ) { expensesList, args ->
        val filteredExpenses = expensesList.filter { expense ->
            val matchesQuery = expense.description.contains(args.query, ignoreCase = true) ||
                expense.category.contains(args.query, ignoreCase = true)
            val matchesCategory = args.category == null || expense.category == args.category
            matchesQuery && matchesCategory
        }

        ExpenseListUIState(
            searchQuery = args.query,
            selectedCategory = args.category,
            currency = args.currency,
            isRefreshing = args.refreshing,
            expenses = filteredExpenses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseListUIState())

    private data class FourArgs(
        val query: String,
        val category: String?,
        val currency: AppCurrency,
        val refreshing: Boolean
    )

    fun onIntent(intent: ExpenseListIntent) {
        when (intent) {
            is ExpenseListIntent.SearchQueryChanged -> _searchQuery.value = intent.query
            is ExpenseListIntent.CategoryFilterChanged -> _selectedCategory.value = intent.category
            is ExpenseListIntent.DeleteExpense -> deleteExpense(intent.id)
            is ExpenseListIntent.UndoDelete -> undoDelete(intent.id)
            ExpenseListIntent.SyncExpenses -> syncExpenses()
        }
    }

    private fun deleteExpense(id: String) {
        viewModelScope.launch {
            deleteExpenseUseCase(id)
            _effect.send(ExpenseListUiEffect.ShowUndoSnackbar(id))
        }
    }

    private fun undoDelete(id: String) {
        viewModelScope.launch {
            undoDeleteExpenseUseCase(id)
        }
    }

    private fun syncExpenses() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncExpensesUseCase()
                .onFailure { error ->
                    _effect.send(ExpenseListUiEffect.ShowError("Sync failed: ${error.message}"))
                }
            _isRefreshing.value = false
        }
    }
}
