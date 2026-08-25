package com.vvv.openexpensetracker.presentation.screens.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.model.Category
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.usecase.GetCurrencyUseCase
import com.vvv.openexpensetracker.domain.usecase.GetExpensesUseCase
import com.vvv.openexpensetracker.domain.usecase.GetLlmStatusUseCase
import com.vvv.openexpensetracker.domain.usecase.SaveExpenseUseCase
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val isSaved: Boolean = false,
    val isScanEnabled: Boolean = false
)

class AddExpenseViewModel(
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    getLlmStatusUseCase: GetLlmStatusUseCase,
    getCurrencyUseCase: GetCurrencyUseCase
) : ViewModel() {

    private var editingExpenseId: String? = null

    private val _uiState = MutableStateFlow(AddExpenseUIState(date = getCurrentTimeMillis()))
    val uiState: StateFlow<AddExpenseUIState> = combine(
        _uiState,
        getCurrencyUseCase.currency,
        getLlmStatusUseCase.isModelDownloadedFlow()
    ) { state, currency, isLlmDownloaded ->
        state.copy(
            currency = currency,
            isScanEnabled = isLlmDownloaded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.copy(isSaved = false))

    private fun getCurrentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }

    fun onIntent(intent: AddExpenseIntent) {
        when (intent) {
            is AddExpenseIntent.LoadExpense -> loadExpense(intent.id)
            is AddExpenseIntent.DescriptionChanged -> onDescriptionChanged(intent.text)
            is AddExpenseIntent.AmountChanged -> onAmountChanged(intent.text)
            is AddExpenseIntent.DateChanged -> onDateChanged(intent.timestamp)
            is AddExpenseIntent.CategoryChanged -> onCategoryChanged(intent.category)
            is AddExpenseIntent.ReceiptScanned -> onReceiptScanned(intent.receipt)
            AddExpenseIntent.SaveExpense -> saveExpense()
            AddExpenseIntent.ResetSaveState -> _uiState.update { it.copy(isSaved = false) }
        }
    }

    private fun loadExpense(id: String?) {
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
        _uiState.update { it.copy(isSaved = false) }
        viewModelScope.launch {
            val list = getExpensesUseCase().firstOrNull() ?: emptyList()
            val expense = list.find { it.id == id }
            if (expense != null) {
                _uiState.update {
                    it.copy(
                        description = expense.description,
                        amount = expense.amount.toString(),
                        date = expense.date,
                        category = expense.category,
                        isSaved = false
                    )
                }
            }
        }
    }

    private fun onDescriptionChanged(text: String) {
        _uiState.update {
            it.copy(
                description = text,
                descriptionError = if (text.isNotBlank()) null else it.descriptionError
            )
        }
    }

    private fun onAmountChanged(text: String) {
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

    private fun onDateChanged(timestamp: Long) {
        _uiState.update { it.copy(date = timestamp) }
    }

    private fun onCategoryChanged(newCategory: String) {
        _uiState.update { it.copy(category = newCategory) }
    }

    private fun onReceiptScanned(receipt: ParsedReceipt) {
        viewModelScope.launch {
            val mappedCategory = mapCategory(receipt.category)
            val richDescription = constructDescription(receipt.merchant, receipt.items)
            
            _uiState.update {
                it.copy(
                    amount = receipt.amount?.let { amt -> formatAmount(amt) } ?: it.amount,
                    date = receipt.date ?: it.date,
                    category = mappedCategory ?: it.category,
                    description = richDescription.ifBlank { it.description },
                    amountError = null,
                    descriptionError = null
                )
            }
        }
    }

    private fun mapCategory(suggested: String?): String? {
        if (suggested == null) return null
        
        val normalized = suggested.trim().uppercase()
        
        // 1. Exact match in Category.list
        Category.list.find { it.uppercase() == normalized }?.let { return it }
        
        // 2. Heuristic mapping
        return when {
            normalized.contains("FOOD") || normalized.contains("GROCERY") || 
            normalized.contains("DINING") || normalized.contains("RESTAURANT") -> Category.FOOD
            normalized.contains("TRANSPORT") || normalized.contains("GAS") || 
            normalized.contains("FUEL") || normalized.contains("TAXI") || normalized.contains("UBER") -> Category.TRANSPORT
            normalized.contains("UTIL") || normalized.contains("BILL") || normalized.contains("ELECTRIC") -> Category.UTILITIES
            normalized.contains("ENT") || normalized.contains("MOVIE") || normalized.contains("GAME") -> Category.ENTERTAINMENT
            normalized.contains("HEALTH") || normalized.contains("MEDICINE") || normalized.contains("DRUG") || normalized.contains("CLINIC") -> Category.HEALTH
            normalized.contains("SHOP") || normalized.contains("STORE") || normalized.contains("CLOTH") -> Category.SHOPPING
            else -> Category.OTHERS
        }
    }

    private fun constructDescription(merchant: String?, items: String?): String {
        return buildString {
            if (!merchant.isNullOrBlank()) {
                append(merchant.trim())
            }
            if (!items.isNullOrBlank()) {
                if (isNotEmpty()) append(" ")
                append("(${items.trim()})")
            }
        }.trim()
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            amount.toString()
        }
    }

    private fun saveExpense() {
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
            saveExpenseUseCase(expense)
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
