package com.vadim.expensetracker.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadim.expensetracker.domain.model.Expense
import com.vadim.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import kotlinx.coroutines.flow.stateIn




class StatsViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository.getExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = expenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryTotals: StateFlow<Map<String, Double>> = expenses.map { list ->
        list.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val monthlyTotals: StateFlow<Map<String, Double>> = expenses.map { list ->
        // Group by month-year
        list.groupBy { formatMonthYear(it.date) }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private fun formatMonthYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val tz = TimeZone.currentSystemDefault()
        val dateTime = instant.toLocalDateTime(tz)
        val localDate = dateTime.date
        // E.g., "05/2026"
        val month = localDate.monthNumber.toString().padStart(2, '0')
        return "$month/${localDate.year}"
    }
}
