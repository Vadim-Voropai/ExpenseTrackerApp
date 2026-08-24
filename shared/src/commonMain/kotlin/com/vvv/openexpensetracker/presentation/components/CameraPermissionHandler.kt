package com.vvv.openexpensetracker.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
)
