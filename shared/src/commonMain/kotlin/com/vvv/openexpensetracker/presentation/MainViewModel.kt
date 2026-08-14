package com.vvv.openexpensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
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
    private val authRepository: GoogleAuthRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<MainUIState> = combine(
        authRepository.userEmail,
        authRepository.userName
    ) { email, name ->
        MainUIState(
            userEmail = email,
            userName = name
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUIState())

    init {
        // Automatic sync at app launch
        viewModelScope.launch {
            if (authRepository.isSignedIn()) {
                expenseRepository.syncWithGoogleDrive()
            }
        }
    }

    fun setSignInHandler(handler: GoogleAuthRepository.SignInHandler?) {
        authRepository.setSignInHandler(handler)
    }

    fun handleSignInResult(data: Any?) {
        authRepository.handleSignInResult(data)
    }

    fun signOut() {
        authRepository.signOut()
    }
}
