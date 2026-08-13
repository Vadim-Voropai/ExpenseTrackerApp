package com.vvv.openexpensetracker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUIState(
    val isSignedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val syncMessage: String? = null,
    val currency: AppCurrency = AppCurrency.USD,
    val lastSyncTime: Long = 0L
)

class SettingsViewModel(
    private val authRepository: GoogleAuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUIState> = combine(
        combine(
            authRepository.accessToken,
            authRepository.userEmail,
            authRepository.userName
        ) { token, email, name -> Triple(token, email, name) },
        combine(
            _isSyncing,
            _syncMessage,
            preferencesRepository.currency
        ) { syncing, message, currency -> Triple(syncing, message, currency) }
    ) { authInfo, prefInfo ->
        val (token, email, name) = authInfo
        val (syncing, message, currency) = prefInfo
        SettingsUIState(
            isSignedIn = token != null,
            isSyncing = syncing,
            userEmail = email,
            userName = name,
            syncMessage = message,
            currency = currency,
            lastSyncTime = expenseRepository.getLastSyncTime()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUIState())

    fun clearMessage() {
        _syncMessage.value = null
    }

    fun signIn() {
        authRepository.initiateSignIn()
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun syncNow() {
        if (!authRepository.isSignedIn()) {
            _syncMessage.value = "Sign in first to sync with Google Drive"
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Syncing..."
            expenseRepository.syncWithGoogleDrive()
                .onSuccess {
                    _syncMessage.value = "Sync completed successfully!"
                }
                .onFailure { error ->
                    _syncMessage.value = "Sync failed: ${error.message}"
                }
            _isSyncing.value = false
        }
    }

    fun setCurrency(newCurrency: AppCurrency) {
        preferencesRepository.setCurrency(newCurrency)
    }
}
