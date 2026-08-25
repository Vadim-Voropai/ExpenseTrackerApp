package com.vvv.openexpensetracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.Foundation.*
import platform.UIKit.*
import platform.Vision.*
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun TextRecognitionCamera(
    modifier: Modifier,
    isPaused: Boolean,
    isReceipt: (String) -> Boolean,
    onTextDetected: (String) -> Unit
) {
    val viewController = remember {
        TextRecognitionViewController().apply {
            this.onTextDetected = onTextDetected
            this.isReceipt = isReceipt
        }
    }

    viewController.isPaused = isPaused
    
    UIKitViewController(
        factory = { viewController },
        modifier = modifier
    )
}

private class TextRecognitionViewController : UIViewController(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    var onTextDetected: ((String) -> Unit)? = null
    var isReceipt: ((String) -> Boolean)? = null
    var isPaused: Boolean = false
    
    private val captureSession = AVCaptureSession()

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setupCamera() {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as? AVCaptureDeviceInput ?: return
        
        if (captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        }

        val output = AVCaptureVideoDataOutput()
        output.setSampleBufferDelegate(this, dispatch_get_main_queue())
        
        if (captureSession.canAddOutput(output)) {
            captureSession.addOutput(output)
        }

        val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(captureSession)
        previewLayer.setFrame(view.bounds)
        previewLayer.setVideoGravity(AVLayerVideoGravityResizeAspectFill)
        view.layer.addSublayer(previewLayer)

        captureSession.startRunning()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        if (isPaused) return
        
        val buffer = didOutputSampleBuffer ?: return
        val handler = VNImageRequestHandler(cMSampleBuffer = buffer, options = emptyMap<Any?, Any?>())
        
        val request = VNRecognizeTextRequest { request, _ ->
            val results = request?.results as? List<VNRecognizedTextObservation>
            val recognizedText = results?.joinToString("\n") { observation ->
                observation.topCandidates(1u).firstOrNull()?.let { (it as? VNRecognizedText)?.string } ?: ""
            }
            if (!recognizedText.isNullOrEmpty() && (isReceipt?.invoke(recognizedText) == true)) {
                onTextDetected?.invoke(recognizedText)
            }
        }
        
        try {
            handler.performRequests(listOf(request), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
