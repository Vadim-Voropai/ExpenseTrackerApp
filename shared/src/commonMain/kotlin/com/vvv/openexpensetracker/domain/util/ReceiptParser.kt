package com.vvv.openexpensetracker.domain.util

data class ParsedReceipt(
    val amount: Double?,
    val date: Long?,
    val category: String?,
    val merchant: String?,
)

object ReceiptParser {

    private val amountRegex = Regex("""(\d+[.,]\d{2})""")
    private val dateRegex = Regex("""(\d{1,2})[./-](\d{1,2})[./-](\d{2,4})""")
    private const val totalKeyword = "TOTAL"
    private const val TRIM_BUFFER = 20

    private val receiptKeywords = listOf(
        "DATE", "DATETIME", "SUBTOTAL", "RECEIPT", "ORDER", "INVOICE", "AUTH", "AMOUNT"
    )

    fun isReceipt(text: String): Boolean {
        val hasKeywords = receiptKeywords.any { text.contains(it, ignoreCase = true) }
        val hasTotal = text.contains(totalKeyword, ignoreCase = true)
        val hasPrice = amountRegex.containsMatchIn(text)
        val hasDate = dateRegex.containsMatchIn(text)
        return hasTotal && hasKeywords && hasPrice && hasDate
    }

    fun trimAfterTotal(text: String): String {
        val totalIndex = text.lastIndexOf(totalKeyword, ignoreCase = true)
        if (totalIndex == -1) return text

        // Look for the first price match after the "TOTAL" keyword
        val afterTotal = text.substring(totalIndex)
        val priceMatch = amountRegex.find(afterTotal) ?: return text

        // End the string after the price match + small safety buffer
        val cutoff = totalIndex + priceMatch.range.last + 1
        return text.take(cutoff + TRIM_BUFFER)
    }
}
