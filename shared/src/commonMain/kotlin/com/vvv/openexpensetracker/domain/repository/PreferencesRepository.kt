package com.vvv.openexpensetracker.domain.repository

import com.vvv.openexpensetracker.domain.model.AppCurrency
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val currency: StateFlow<AppCurrency>
    fun setCurrency(newCurrency: AppCurrency)
    
    val isLlmDownloaded: StateFlow<Boolean>
    fun setLlmDownloaded(downloaded: Boolean)

    val llmBenchmarkResult: StateFlow<LlmBenchmarkResult?>
    fun setLlmBenchmarkResult(result: LlmBenchmarkResult?)
}
