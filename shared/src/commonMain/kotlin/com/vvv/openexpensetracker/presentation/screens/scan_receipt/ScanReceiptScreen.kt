package com.vvv.openexpensetracker.presentation.screens.scan_receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import com.vvv.openexpensetracker.presentation.components.CameraPermissionHandler
import com.vvv.openexpensetracker.presentation.components.TextRecognitionCamera
import kotlinx.coroutines.flow.collectLatest
import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.back
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(
    viewModel: ScanReceiptViewModel,
    onReceiptDetected: (ParsedReceipt) -> Unit,
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ScanReceiptUiEffect.ReceiptFound -> {
                    onReceiptDetected(effect.parsed)
                    navigateBack()
                }
                is ScanReceiptUiEffect.ShowError -> {
                    // Feedback handled via state or separate event if needed
                }
            }
        }
    }

    CameraPermissionHandler(
        onPermissionGranted = { isPermissionGranted = true },
        onPermissionDenied = { navigateBack() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isInitializing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("Initializing AI Engine...", color = Color.White)
                    }
                }
            } else if (isPermissionGranted) {
                TextRecognitionCamera(
                    modifier = Modifier.fillMaxSize(),
                    isPaused = uiState.isProcessing,
                    isReceipt = viewModel::isReceipt,
                    onTextDetected = { text ->
                        viewModel.onIntent(ScanReceiptIntent.TextDetected(text))
                    }
                )
                
                // Status Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isProcessing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI is analyzing...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DetectionStatusItem(label = "Amount", isFound = uiState.amountFound)
                            Spacer(modifier = Modifier.width(24.dp))
                            DetectionStatusItem(label = "Date", isFound = uiState.dateFound)
                        }
                        Text(
                            text = "Align receipt to capture all details",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Waiting for camera permission...", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DetectionStatusItem(label: String, isFound: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isFound) Color.Green else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (isFound) Color.Green else Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
