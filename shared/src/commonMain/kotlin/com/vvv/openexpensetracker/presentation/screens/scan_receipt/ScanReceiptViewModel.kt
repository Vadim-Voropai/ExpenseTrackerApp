package com.vvv.openexpensetracker.presentation.screens.scan_receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.usecase.AnalyzeReceiptLlmUseCase
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanReceiptUiEffect {
    data class ReceiptFound(val text: String, val parsed: ParsedReceipt) : ScanReceiptUiEffect
    data class ShowError(val message: String) : ScanReceiptUiEffect
}

data class ScanReceiptUIState(
    val isInitializing: Boolean = false,
    val isReady: Boolean = false,
    val isProcessing: Boolean = false,
    val amountFound: Boolean = false,
    val dateFound: Boolean = false
)

class ScanReceiptViewModel(
    private val analyzeReceiptLlmUseCase: AnalyzeReceiptLlmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanReceiptUIState())
    val uiState: StateFlow<ScanReceiptUIState> = _uiState.asStateFlow()

    private val _effect = Channel<ScanReceiptUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        initializeLlm()
    }

    fun onIntent(intent: ScanReceiptIntent) {
        when (intent) {
            is ScanReceiptIntent.TextDetected -> processText(intent.text)
            else -> {} // Download handled in settings now
        }
    }

    private fun initializeLlm() {
        _uiState.update { it.copy(isInitializing = true) }
        viewModelScope.launch {
            val success = analyzeReceiptLlmUseCase.initialize()
            _uiState.update { it.copy(isInitializing = false, isReady = success) }
            if (!success) {
                _effect.send(ScanReceiptUiEffect.ShowError("Failed to initialize LLM"))
            }
        }
    }

    private fun processText(text: String) {
        if (!_uiState.value.isReady || _uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            if (analyzeReceiptLlmUseCase.isReceipt(text)) {
                val parsed = analyzeReceiptLlmUseCase.extractData(text)
                if (parsed != null) {
                    _uiState.update {
                        it.copy(
                            amountFound = parsed.amount != null,
                            dateFound = parsed.date != null
                        )
                    }
                    
                    if (parsed.amount != null && parsed.date != null) {
                        _effect.send(ScanReceiptUiEffect.ReceiptFound(text, parsed))
                    }
                }
            }
            
            _uiState.update { it.copy(isProcessing = false) }
        }
    }
}
