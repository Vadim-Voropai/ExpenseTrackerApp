package com.vvv.openexpensetracker.data.repository

import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesRepositoryImpl : PreferencesRepository {
    private val _currency = MutableStateFlow(AppCurrency.USD)
    override val currency: StateFlow<AppCurrency> = _currency

    override fun setCurrency(newCurrency: AppCurrency) {
        _currency.value = newCurrency
    }
}
