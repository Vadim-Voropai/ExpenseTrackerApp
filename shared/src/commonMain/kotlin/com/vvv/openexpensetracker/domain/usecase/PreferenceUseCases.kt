package com.vvv.openexpensetracker.domain.usecase

import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow

class GetCurrencyUseCase(private val repository: PreferencesRepository) {
    val currency: StateFlow<AppCurrency> = repository.currency
}

class SetCurrencyUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(currency: AppCurrency) = repository.setCurrency(currency)
}
