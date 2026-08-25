package com.vvv.openexpensetracker.data.repository

import com.vvv.openexpensetracker.core.Constants
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.repository.LlmBenchmarkResult
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesRepositoryImpl(
    private val localStorage: LocalStorage
) : PreferencesRepository {
    
    private val _currency = MutableStateFlow(loadCurrency())
    override val currency: StateFlow<AppCurrency> = _currency

    private val _isLlmDownloaded = MutableStateFlow(loadLlmStatus())
    override val isLlmDownloaded: StateFlow<Boolean> = _isLlmDownloaded

    private val _llmBenchmarkResult = MutableStateFlow(loadBenchmarkResult())
    override val llmBenchmarkResult: StateFlow<LlmBenchmarkResult?> = _llmBenchmarkResult

    override fun setCurrency(newCurrency: AppCurrency) {
        localStorage.saveString("app_currency", newCurrency.name)
        _currency.value = newCurrency
    }

    override fun setLlmDownloaded(downloaded: Boolean) {
        localStorage.saveString(Constants.KEY_LLM_DOWNLOADED, downloaded.toString())
        _isLlmDownloaded.value = downloaded
    }

    override fun setLlmBenchmarkResult(result: LlmBenchmarkResult?) {
        if (result == null) {
            localStorage.saveString(Constants.KEY_LLM_TPS, "")
            localStorage.saveString(Constants.KEY_LLM_BENCHMARK_DURATION, "")
        } else {
            localStorage.saveString(Constants.KEY_LLM_TPS, result.tps.toString())
            localStorage.saveString(Constants.KEY_LLM_BENCHMARK_DURATION, result.durationMs.toString())
        }
        _llmBenchmarkResult.value = result
    }

    private fun loadCurrency(): AppCurrency {
        val saved = localStorage.getString("app_currency")
        return try {
            AppCurrency.valueOf(saved ?: AppCurrency.USD.name)
        } catch (_: Exception) {
            AppCurrency.USD
        }
    }

    private fun loadLlmStatus(): Boolean {
        return localStorage.getString(Constants.KEY_LLM_DOWNLOADED)?.toBoolean() ?: false
    }

    private fun loadBenchmarkResult(): LlmBenchmarkResult? {
        val tps = localStorage.getString(Constants.KEY_LLM_TPS)?.toDoubleOrNull()
        val duration = localStorage.getString(Constants.KEY_LLM_BENCHMARK_DURATION)?.toLongOrNull()
        return if (tps != null && duration != null) {
            LlmBenchmarkResult(tps, duration)
        } else null
    }
}
