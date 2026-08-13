package com.vvv.openexpensetracker.domain.model

enum class AppCurrency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£")
}
