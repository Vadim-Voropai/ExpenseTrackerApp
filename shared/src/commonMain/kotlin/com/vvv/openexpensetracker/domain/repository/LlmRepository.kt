package com.vvv.openexpensetracker.domain.repository

import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import kotlinx.coroutines.flow.Flow

data class LlmBenchmarkResult(
    val tps: Double,
    val durationMs: Long
)

interface LlmRepository {
    fun isModelDownloadedFlow(): Flow<Boolean>
    suspend fun downloadModel(): Flow<Float>
    suspend fun initialize(): Boolean
    fun getModelName(): String
    fun deleteModel(): Boolean
    suspend fun extractReceiptData(text: String): ParsedReceipt?
}
