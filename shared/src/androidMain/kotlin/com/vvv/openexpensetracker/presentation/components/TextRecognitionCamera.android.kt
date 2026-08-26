package com.vvv.openexpensetracker.presentation.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

@Composable
actual fun TextRecognitionCamera(
    modifier: Modifier,
    isPaused: Boolean,
    isReceipt: (String) -> Boolean,
    onTextDetected: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val mainExecutor = remember { Dispatchers.Main.asExecutor() }

    LaunchedEffect(isPaused) {
        if (isPaused) {
            controller.clearImageAnalysisAnalyzer()
        } else {
            controller.setImageAnalysisAnalyzer(
                mainExecutor,
                MlKitAnalyzer(
                    listOf(textRecognizer),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    mainExecutor
                ) { result ->
                    val visionText = result.getValue(textRecognizer)
                    if (visionText != null && visionText.text.isNotEmpty() && isReceipt(visionText.text)) {
                        onTextDetected(visionText.text)
                    }
                }
            )
        }
    }

    controller.bindToLifecycle(lifecycleOwner)

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = controller
            }
        },
        modifier = modifier
    )
}
