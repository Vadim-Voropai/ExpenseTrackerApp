package com.vadim.expensetracker.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadim.expensetracker.domain.model.Expense
import com.vadim.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseListViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    val expenses: StateFlow<List<Expense>> = combine(
        repository.getExpenses(),
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { expense ->
            val matchesQuery = expense.description.contains(query, ignoreCase = true) ||
                    expense.category.contains(query, ignoreCase = true)
            val matchesCategory = category == null || expense.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
