package com.vvv.openexpensetracker.presentation.screens.settings

import com.vvv.openexpensetracker.domain.model.AppCurrency

sealed interface SettingsIntent {
    data object SignIn : SettingsIntent
    data object SignOut : SettingsIntent
    data object SyncNow : SettingsIntent
    data class SetCurrency(val currency: AppCurrency) : SettingsIntent
    data object DownloadLlmModel : SettingsIntent
    data object DeleteLlmModel : SettingsIntent
}
