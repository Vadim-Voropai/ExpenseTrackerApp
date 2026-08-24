package com.vvv.openexpensetracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                onPermissionGranted()
            }
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        if (granted) onPermissionGranted() else onPermissionDenied()
                    }
                }
            }
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                onPermissionDenied()
            }
            else -> {
                onPermissionDenied()
            }
        }
    }
}
