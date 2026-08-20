package com.vvv.openexpensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.usecase.GetAuthStateUseCase
import com.vvv.openexpensetracker.domain.usecase.HandleSignInResultUseCase
import com.vvv.openexpensetracker.domain.usecase.SyncExpensesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUIState(
    val userEmail: String? = null,
    val userName: String? = null
)

class MainViewModel(
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val handleSignInResultUseCase: HandleSignInResultUseCase,
    private val syncExpensesUseCase: SyncExpensesUseCase,
    private val authRepository: GoogleAuthRepository // Still needed for signInHandler
) : ViewModel() {

    val uiState: StateFlow<MainUIState> = combine(
        getAuthStateUseCase.userEmail,
        getAuthStateUseCase.userName
    ) { email, name ->
        MainUIState(
            userEmail = email,
            userName = name
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUIState())

    init {
        // Automatic sync at app launch
        viewModelScope.launch {
            if (getAuthStateUseCase.isSignedIn()) {
                syncExpensesUseCase()
            }
        }
    }

    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.HandleSignInResult -> handleSignInResult(intent.data)
        }
    }

    fun setSignInHandler(handler: GoogleAuthRepository.SignInHandler?) {
        authRepository.setSignInHandler(handler)
    }

    private fun handleSignInResult(data: Any?) {
        handleSignInResultUseCase(data)
    }
}
