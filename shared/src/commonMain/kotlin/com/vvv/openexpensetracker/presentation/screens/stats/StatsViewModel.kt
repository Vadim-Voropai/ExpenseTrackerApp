package com.vvv.openexpensetracker.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class StatsUIState(
    val totalSpent: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap(),
    val monthlyTotals: Map<String, Double> = emptyMap()
)

class StatsViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUIState> = repository.getExpenses().map { expenses ->
        val total = expenses.sumOf { it.amount }
        val categories = expenses.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
        val monthly = expenses.groupBy { formatMonthYear(it.date) }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        StatsUIState(
            totalSpent = total,
            categoryTotals = categories,
            monthlyTotals = monthly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUIState())

    private fun formatMonthYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val tz = TimeZone.currentSystemDefault()
        val dateTime = instant.toLocalDateTime(tz)
        val localDate = dateTime.date
        val month = localDate.monthNumber.toString().padStart(2, '0')
        return "$month/${localDate.year}"
    }
}
