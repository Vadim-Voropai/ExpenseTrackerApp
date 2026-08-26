package com.vvv.openexpensetracker.domain.usecase

import com.vvv.openexpensetracker.domain.repository.LlmRepository
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import kotlinx.coroutines.flow.Flow

class GetLlmStatusUseCase(private val repository: LlmRepository) {
    fun isModelDownloadedFlow(): Flow<Boolean> = repository.isModelDownloadedFlow()
    fun getModelName(): String = repository.getModelName()
}

class DownloadLlmModelUseCase(private val repository: LlmRepository) {
    suspend operator fun invoke(): Flow<Float> = repository.downloadModel()
}

class DeleteLlmModelUseCase(private val repository: LlmRepository) {
    operator fun invoke(): Boolean = repository.deleteModel()
}

class InitializeLlmUseCase(private val repository: LlmRepository) {
    suspend operator fun invoke(): Boolean = repository.initialize()
}

class NormalizeReceiptLlmUseCase(private val repository: LlmRepository) {
    suspend operator fun invoke(text: String): String = repository.normalizeReceiptData(text)
}

class AnalyzeReceiptLlmUseCase(private val repository: LlmRepository) {
    suspend operator fun invoke(text: String): ParsedReceipt? = repository.extractReceiptData(text)
}
