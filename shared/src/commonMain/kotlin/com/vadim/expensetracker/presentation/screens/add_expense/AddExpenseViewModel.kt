package com.vadim.expensetracker.presentation.screens.add_expense

import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock.System
import androidx.lifecycle.viewModelScope
import com.vadim.expensetracker.domain.model.Category
import com.vadim.expensetracker.domain.model.Expense
import com.vadim.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class AddExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private var editingExpenseId: String? = null

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _date = MutableStateFlow(0L)
    val date = _date.asStateFlow()

    private val _category = MutableStateFlow<String>(Category.FOOD)
    val category = _category.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError = _descriptionError.asStateFlow()

    private val _amountError = MutableStateFlow<String?>(null)
    val amountError = _amountError.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    init {
        _date.value = getCurrentTimeMillis()
    }

    private fun getCurrentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }

    fun loadExpense(id: String?) {
        if (id == null) {
            editingExpenseId = null
            _description.value = ""
            _amount.value = ""
            _date.value = getCurrentTimeMillis()
            _category.value = Category.FOOD
            _isSaved.value = false
            return
        }
        editingExpenseId = id
        viewModelScope.launch {
            val list = repository.getExpenses().firstOrNull() ?: emptyList()
            val expense = list.find { it.id == id }
            if (expense != null) {
                _description.value = expense.description
                _amount.value = expense.amount.toString()
                _date.value = expense.date
                _category.value = expense.category
            }
        }
    }

    fun onDescriptionChanged(text: String) {
        _description.value = text
        if (text.isNotBlank()) _descriptionError.value = null
    }

    fun onAmountChanged(text: String) {
        // Only allow numbers and one decimal dot
        val filtered = text.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && text.indexOf('.') == index)
        }
        _amount.value = filtered
        if (filtered.isNotBlank()) _amountError.value = null
    }

    fun onDateChanged(timestamp: Long) {
        _date.value = timestamp
    }

    fun onCategoryChanged(newCategory: String) {
        _category.value = newCategory
    }

    fun saveExpense() {
        val desc = _description.value.trim()
        val amtStr = _amount.value.trim()

        var isValid = true
        if (desc.isBlank()) {
            _descriptionError.value = "Description is required"
            isValid = false
        }
        val amt = amtStr.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            _amountError.value = "Enter a valid amount greater than 0"
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            val now = getCurrentTimeMillis()
            val expense = Expense(
                id = editingExpenseId ?: generateId(),
                description = desc,
                amount = amt ?: 0.0,
                date = _date.value,
                category = _category.value,
                lastModified = now
            )
            repository.saveExpense(expense)
            _isSaved.value = true
        }
    }

    private fun generateId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}
