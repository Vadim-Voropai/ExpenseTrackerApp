package com.vvv.openexpensetracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface GoogleAuthRepository {
    val accessToken: StateFlow<String?>
    val userEmail: StateFlow<String?>
    val userName: StateFlow<String?>

    fun initiateSignIn()
    suspend fun signOut()
    fun setSession(token: String?, email: String?, name: String?)
    fun isSignedIn(): Boolean
    suspend fun fetchProfileAndSetSession(token: String)
    suspend fun refreshAccessToken(): String?
    fun setSignInHandler(handler: SignInHandler?)
    fun handleSignInResult(data: Any?)

    interface SignInHandler {
        fun onSignInRequested()
        fun onSignOutRequested()
        fun onLaunchIntent(intentSender: Any)
    }
}
