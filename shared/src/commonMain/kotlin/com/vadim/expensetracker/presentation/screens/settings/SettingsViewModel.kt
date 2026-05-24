package com.vadim.expensetracker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadim.expensetracker.domain.repository.ExpenseRepository
import com.vadim.expensetracker.domain.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: GoogleAuthRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val isSignedIn: StateFlow<Boolean> = MutableStateFlow(authRepository.isSignedIn())
        .apply {
            // Keep updated by watching token
            viewModelScope.launch {
                authRepository.accessToken.collect {
                    value = it != null
                }
            }
        }

    val userEmail: StateFlow<String?> = authRepository.userEmail
    val userName: StateFlow<String?> = authRepository.userName

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

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

    fun getLastSyncTime(): Long {
        return expenseRepository.getLastSyncTime()
    }

    fun clearMessage() {
        _syncMessage.value = null
    }
}
