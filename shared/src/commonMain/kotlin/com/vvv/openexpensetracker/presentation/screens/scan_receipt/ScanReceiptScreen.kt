package com.vvv.openexpensetracker.presentation.screens.scan_receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import com.vvv.openexpensetracker.presentation.components.CameraPermissionHandler
import com.vvv.openexpensetracker.presentation.components.TextRecognitionCamera
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest
import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.back
import openexpensetracker.shared.generated.resources.scan_analyzing
import openexpensetracker.shared.generated.resources.scan_guidance
import openexpensetracker.shared.generated.resources.scan_initializing
import openexpensetracker.shared.generated.resources.scan_label_amount
import openexpensetracker.shared.generated.resources.scan_label_date
import openexpensetracker.shared.generated.resources.scan_title
import openexpensetracker.shared.generated.resources.scan_waiting_permission
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
    val dimens = AppTheme.dimens

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
                title = { Text(stringResource(Res.string.scan_title)) },
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
                        Spacer(modifier = Modifier.padding(dimens.spacingSmall))
                        Text(stringResource(Res.string.scan_initializing), color = Color.White)
                    }
                }
            } else if (isPermissionGranted) {
                Box(modifier = Modifier.fillMaxSize()) {
                    TextRecognitionCamera(
                        modifier = Modifier.fillMaxSize(),
                        isPaused = uiState.isProcessing,
                        isReceipt = viewModel::isReceipt,
                        onTextDetected = { text ->
                            viewModel.onIntent(ScanReceiptIntent.TextDetected(text))
                        }
                    )

                    if (uiState.isProcessing) {
                        // Centered Processing Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(dimens.iconSizeExtraLarge),
                                    strokeWidth = dimens.strokeWidthSmall
                                )
                                Spacer(modifier = Modifier.height(dimens.spacingNormal))
                                Text(
                                    text = stringResource(Res.string.scan_analyzing),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    } else {
                        // Status Overlay (Only show when not processing)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(dimens.spacingNormal),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DetectionStatusItem(
                                    label = stringResource(Res.string.scan_label_amount),
                                    isFound = uiState.amountFound
                                )
                                Spacer(modifier = Modifier.width(dimens.spacingLarge))
                                DetectionStatusItem(
                                    label = stringResource(Res.string.scan_label_date),
                                    isFound = uiState.dateFound
                                )
                            }
                            Text(
                                text = stringResource(Res.string.scan_guidance),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = dimens.spacingSmall)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.scan_waiting_permission), color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DetectionStatusItem(label: String, isFound: Boolean) {
    val dimens = AppTheme.dimens
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isFound) Color.Green else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(dimens.iconSizeStatus)
        )
        Spacer(modifier = Modifier.width(dimens.spacingSmall))
        Text(
            text = label,
            color = if (isFound) Color.Green else Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
