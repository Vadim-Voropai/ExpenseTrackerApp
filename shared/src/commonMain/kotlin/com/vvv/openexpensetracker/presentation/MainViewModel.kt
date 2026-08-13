package com.vvv.openexpensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainUIState(
    val userEmail: String? = null,
    val userName: String? = null
)

class MainViewModel(
    private val authRepository: GoogleAuthRepository
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
