package com.vvv.openexpensetracker.presentation.screens.scan_receipt

sealed interface ScanReceiptIntent {
    data object DownloadModel : ScanReceiptIntent
    data class TextDetected(val text: String) : ScanReceiptIntent
}
