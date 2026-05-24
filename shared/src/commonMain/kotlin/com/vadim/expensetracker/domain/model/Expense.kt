package com.vadim.expensetracker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val date: Long, // milliseconds timestamp
    val category: String,
    val lastModified: Long // milliseconds timestamp for syncing conflict resolution
)
