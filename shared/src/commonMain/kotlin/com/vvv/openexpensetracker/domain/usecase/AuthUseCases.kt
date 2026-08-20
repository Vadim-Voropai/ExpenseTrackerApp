package com.vvv.openexpensetracker.domain.usecase

import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.StateFlow

class SignInUseCase(private val repository: GoogleAuthRepository) {
    operator fun invoke() = repository.initiateSignIn()
}

class SignOutUseCase(private val repository: GoogleAuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}

class GetAuthStateUseCase(private val repository: GoogleAuthRepository) {
    val accessToken: StateFlow<String?> = repository.accessToken
    val userEmail: StateFlow<String?> = repository.userEmail
    val userName: StateFlow<String?> = repository.userName
    
    fun isSignedIn(): Boolean = repository.isSignedIn()
}

class HandleSignInResultUseCase(private val repository: GoogleAuthRepository) {
    operator fun invoke(data: Any?) = repository.handleSignInResult(data)
}
