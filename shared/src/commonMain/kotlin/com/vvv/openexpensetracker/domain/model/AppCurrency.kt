package com.vvv.openexpensetracker.domain.model

enum class AppCurrency(val code: String, val symbol: String) {
    CAD("CAD", "$"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥"),
    AUD("AUD", "$"),
    CHF("CHF", "₣"),
    CNY("CNY", "¥"),
}
