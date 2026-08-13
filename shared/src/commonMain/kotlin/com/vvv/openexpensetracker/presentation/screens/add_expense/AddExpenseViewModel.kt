package com.vvv.openexpensetracker.presentation.screens.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.model.Category
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddExpenseUIState(
    val currency: AppCurrency = AppCurrency.USD,
    val description: String = "",
    val amount: String = "",
    val date: Long = 0L,
    val category: String = Category.FOOD,
    val descriptionError: String? = null,
    val amountError: String? = null,
    val isSaved: Boolean = false
)

class AddExpenseViewModel(
    private val repository: ExpenseRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private var editingExpenseId: String? = null

    private val _uiState = MutableStateFlow(AddExpenseUIState(date = getCurrentTimeMillis()))
    val uiState: StateFlow<AddExpenseUIState> = combine(
        _uiState,
        preferencesRepository.currency
    ) { state, currency ->
        state.copy(currency = currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value)

    private fun getCurrentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }

    fun loadExpense(id: String?) {
        if (id == null) {
            editingExpenseId = null
            _uiState.update {
                it.copy(
                    description = "",
                    amount = "",
                    date = getCurrentTimeMillis(),
                    category = Category.FOOD,
                    isSaved = false,
                    descriptionError = null,
                    amountError = null
                )
            }
            return
        }
        editingExpenseId = id
        viewModelScope.launch {
            val list = repository.getExpenses().firstOrNull() ?: emptyList()
            val expense = list.find { it.id == id }
            if (expense != null) {
                _uiState.update {
                    it.copy(
                        description = expense.description,
                        amount = expense.amount.toString(),
                        date = expense.date,
                        category = expense.category
                    )
                }
            }
        }
    }

    fun onDescriptionChanged(text: String) {
        _uiState.update {
            it.copy(
                description = text,
                descriptionError = if (text.isNotBlank()) null else it.descriptionError
            )
        }
    }

    fun onAmountChanged(text: String) {
        val filtered = text.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && text.indexOf('.') == index)
        }
        _uiState.update {
            it.copy(
                amount = filtered,
                amountError = if (filtered.isNotBlank()) null else it.amountError
            )
        }
    }

    fun onDateChanged(timestamp: Long) {
        _uiState.update { it.copy(date = timestamp) }
    }

    fun onCategoryChanged(newCategory: String) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun saveExpense() {
        val desc = _uiState.value.description.trim()
        val amtStr = _uiState.value.amount.trim()

        var isValid = true
        val amt = amtStr.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount greater than 0") }
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            val now = getCurrentTimeMillis()
            val expense = Expense(
                id = editingExpenseId ?: generateId(),
                description = desc,
                amount = amt ?: 0.0,
                date = _uiState.value.date,
                category = _uiState.value.category,
                lastModified = now
            )
            repository.saveExpense(expense)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun generateId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}
