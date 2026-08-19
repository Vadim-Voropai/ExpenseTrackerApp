package com.vvv.openexpensetracker.domain.repository

import com.vvv.openexpensetracker.core.Constants
import com.vvv.openexpensetracker.core.network.get
import com.vvv.openexpensetracker.data.model.remote.GoogleUserInfo
import com.vvv.openexpensetracker.data.source.local.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoogleAuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage
) : GoogleAuthRepository {
    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    override val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    override val userName: StateFlow<String?> = _userName.asStateFlow()

    private var signInHandler: GoogleAuthRepository.SignInHandler? = null

    init {
        CoroutineScope(Dispatchers.Main).launch {
            loadSession()
        }
    }

    private suspend fun loadSession() {
        val token = secureStorage.getString(Constants.KEY_AUTH_TOKEN)
        val email = secureStorage.getString(Constants.KEY_USER_EMAIL)
        val name = secureStorage.getString(Constants.KEY_USER_NAME)
        if (token != null) {
            _accessToken.value = token
            _userEmail.value = email
            _userName.value = name
        }
    }

    override fun setSignInHandler(handler: GoogleAuthRepository.SignInHandler?) {
        this.signInHandler = handler
    }

    override fun initiateSignIn() {
        signInHandler?.onSignInRequested()
    }

    override fun handleSignInResult(data: Any?) {
        // Not used on iOS currently as we simulate sign in from Swift
    }

    override suspend fun signOut() {
        CoroutineScope(Dispatchers.Main).launch {
            setSession(null, null, null)
        }
        signInHandler?.onSignOutRequested()
    }

    override fun setSession(token: String?, email: String?, name: String?) {
        _accessToken.value = token
        _userEmail.value = email
        _userName.value = name

        CoroutineScope(Dispatchers.Main).launch {
            secureStorage.saveString(Constants.KEY_AUTH_TOKEN, token)
            secureStorage.saveString(Constants.KEY_USER_EMAIL, email)
            secureStorage.saveString(Constants.KEY_USER_NAME, name)
        }
    }

    override fun isSignedIn(): Boolean {
        return _accessToken.value != null
    }

    override suspend fun fetchProfileAndSetSession(token: String) {
        try {
            val userInfo: GoogleUserInfo = httpClient.get(Constants.GOOGLE_USERINFO_URL) {
                header("Authorization", "Bearer $token")
            }
            setSession(token, userInfo.email, userInfo.name)
        } catch (e: Exception) {
            e.printStackTrace()
            setSession(token, Constants.AUTH_FALLBACK_EMAIL, Constants.AUTH_FALLBACK_NAME)
        }
    }

    override suspend fun refreshAccessToken(): String? {
        // Silent refresh on iOS would involve GIDSignIn.sharedInstance.restorePreviousSignIn
        // For now, since it's mocked, we just return the existing token
        return _accessToken.value
    }
}
