package com.vvv.openexpensetracker.domain.model

// String constants for expense categories
object Category {
    const val FOOD = "Food"
    const val TRANSPORT = "Transport"
    const val UTILITIES = "Utilities"
    const val ENTERTAINMENT = "Entertainment"
    const val HEALTH = "Health"
    const val SHOPPING = "Shopping"
    const val OTHERS = "Others"

    val list = listOf(FOOD, TRANSPORT, UTILITIES, ENTERTAINMENT, HEALTH, SHOPPING, OTHERS)
}
