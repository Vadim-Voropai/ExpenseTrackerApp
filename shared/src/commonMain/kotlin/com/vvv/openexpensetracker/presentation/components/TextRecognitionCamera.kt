package com.vvv.openexpensetracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TextRecognitionCamera(
    modifier: Modifier = Modifier,
    isPaused: Boolean = false,
    isReceipt: (String) -> Boolean,
    onTextDetected: (String) -> Unit,
)
