package com.vvv.openexpensetracker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.domain.usecase.GetAuthStateUseCase
import com.vvv.openexpensetracker.domain.usecase.GetCurrencyUseCase
import com.vvv.openexpensetracker.domain.usecase.GetLastSyncTimeUseCase
import com.vvv.openexpensetracker.domain.usecase.SetCurrencyUseCase
import com.vvv.openexpensetracker.domain.usecase.SignInUseCase
import com.vvv.openexpensetracker.domain.usecase.SignOutUseCase
import com.vvv.openexpensetracker.domain.usecase.SyncExpensesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiEffect {
    data class ShowSnackbar(val message: String) : SettingsUiEffect
}

data class SettingsUIState(
    val isSignedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val currency: AppCurrency = AppCurrency.USD,
    val lastSyncTime: Long = 0L
)

class SettingsViewModel(
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val syncExpensesUseCase: SyncExpensesUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getLastSyncTimeUseCase: GetLastSyncTimeUseCase
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _effect = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<SettingsUIState> = combine(
        combine(
            getAuthStateUseCase.accessToken,
            getAuthStateUseCase.userEmail,
            getAuthStateUseCase.userName
        ) { token, email, name -> Triple(token, email, name) },
        combine(
            _isSyncing,
            getCurrencyUseCase.currency
        ) { syncing, currency -> Pair(syncing, currency) }
    ) { authInfo, prefInfo ->
        val (token, email, name) = authInfo
        val (syncing, currency) = prefInfo
        SettingsUIState(
            isSignedIn = token != null,
            isSyncing = syncing,
            userEmail = email,
            userName = name,
            currency = currency,
            lastSyncTime = getLastSyncTimeUseCase()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUIState())

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.SignIn -> signIn()
            SettingsIntent.SignOut -> signOut()
            SettingsIntent.SyncNow -> syncNow()
            is SettingsIntent.SetCurrency -> setCurrency(intent.currency)
        }
    }

    private fun signIn() {
        signInUseCase()
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    private fun syncNow() {
        if (!getAuthStateUseCase.isSignedIn()) {
            viewModelScope.launch {
                _effect.send(SettingsUiEffect.ShowSnackbar("Sign in first to sync with Google Drive"))
            }
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            syncExpensesUseCase()
                .onFailure { error ->
                    _effect.send(SettingsUiEffect.ShowSnackbar("Sync failed: ${error.message}"))
                }
            _isSyncing.value = false
        }
    }

    private fun setCurrency(newCurrency: AppCurrency) {
        setCurrencyUseCase(newCurrency)
    }
}
