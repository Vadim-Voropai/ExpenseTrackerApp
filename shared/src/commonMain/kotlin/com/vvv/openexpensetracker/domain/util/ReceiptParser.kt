package com.vvv.openexpensetracker.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

data class ParsedReceipt(
    val amount: Double?,
    val date: Long?,
    val merchant: String?
)

object ReceiptParser {

    private val amountRegex = Regex("""(\d+[.,]\d{2})""")
    private val dateRegex = Regex("""(\d{1,2})[./-](\d{1,2})[./-](\d{2,4})""")

    fun parse(text: String): ParsedReceipt {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        
        val amounts = amountRegex.findAll(text)
            .mapNotNull { it.groupValues[1].replace(',', '.').toDoubleOrNull() }
            .toList()
        
        // Usually the total is the largest number on the receipt
        val amount = amounts.maxOrNull()

        val dateMatch = dateRegex.find(text)
        val date = dateMatch?.let {
            try {
                val day = it.groupValues[1].toInt()
                val month = it.groupValues[2].toInt()
                var year = it.groupValues[3].toInt()
                if (year < 100) year += 2000
                
                val localDate = LocalDate(year, month, day)
                val instant = localDate.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
                instant.toEpochMilliseconds()
            } catch (_: Exception) {
                null
            }
        }

        // Heuristic: Merchant is often the first non-numeric line
        val merchant = lines.firstOrNull { line ->
            !line.any { it.isDigit() } && line.length > 2
        }

        return ParsedReceipt(amount, date, merchant)
    }
}
