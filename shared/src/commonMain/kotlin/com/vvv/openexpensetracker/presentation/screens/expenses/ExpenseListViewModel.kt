package com.vvv.openexpensetracker.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseListUIState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val currency: AppCurrency = AppCurrency.USD,
    val isRefreshing: Boolean = false,
    val syncMessage: String? = null,
    val expenses: List<Expense> = emptyList()
)

class ExpenseListViewModel(
    private val repository: ExpenseRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExpenseListUIState> = combine(
        repository.getExpenses(),
        combine(
            _searchQuery,
            _selectedCategory,
            preferencesRepository.currency,
            _isRefreshing,
            _syncMessage
        ) { query, category, currency, refreshing, message ->
            FiveArgs(query, category, currency, refreshing, message)
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
            syncMessage = args.message,
            expenses = filteredExpenses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseListUIState())

    private data class FiveArgs(
        val query: String,
        val category: String?,
        val currency: AppCurrency,
        val refreshing: Boolean,
        val message: String?
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    fun undoDelete(id: String) {
        viewModelScope.launch {
            repository.undoDeleteExpense(id)
        }
    }

    fun syncExpenses() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncWithGoogleDrive()
                .onSuccess {
                    _syncMessage.value = "Synced successfully!"
                }
                .onFailure { error ->
                    _syncMessage.value = "Sync failed: ${error.message}"
                }
            _isRefreshing.value = false
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun getLastSyncTime(): Long {
        return repository.getLastSyncTime()
    }
}
