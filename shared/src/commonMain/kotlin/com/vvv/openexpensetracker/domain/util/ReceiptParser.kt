package com.vvv.openexpensetracker.domain.util

data class ParsedReceipt(
    val amount: Double?,
    val date: Long?,
    val category: String?,
    val merchant: String?,
    val items: String?
)

object ReceiptParser {

    private val amountRegex = Regex("""(\d+[.,]\d{2})""")
    private val dateRegex = Regex("""(\d{1,2})[./-](\d{1,2})[./-](\d{2,4})""")

    private val receiptKeywords = listOf(
        "TOTAL", "DATE", "DATETIME", "SUBTOTAL", "TAX", "VAT", "CASH", "CARD", "VISA", "MASTERCARD",
        "RECEIPT", "ORDER", "INVOICE", "AUTH", "CHANGE", "NET", "GROSS", "MERCHANT"
    )

    fun isReceipt(text: String): Boolean {
        val hasKeywords = receiptKeywords.any { text.contains(it, ignoreCase = true) }
        val hasPrice = amountRegex.containsMatchIn(text)
        val hasDate = dateRegex.containsMatchIn(text)
        return hasKeywords && hasPrice && hasDate
    }
}
