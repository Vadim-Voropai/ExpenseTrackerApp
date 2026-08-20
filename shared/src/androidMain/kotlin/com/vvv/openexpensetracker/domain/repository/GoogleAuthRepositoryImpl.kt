package com.vvv.openexpensetracker.domain.repository

import android.content.Context
import android.content.Intent
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
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
import kotlinx.coroutines.tasks.await

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage,
) : GoogleAuthRepository {
    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    override val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    override val userName: StateFlow<String?> = _userName.asStateFlow()

    private var signInHandler: GoogleAuthRepository.SignInHandler? = null

    private val credentialManager = CredentialManager.create(context)

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
        val requestedScopes = listOf(
            Scope(Constants.SCOPE_DRIVE_FILE),
            Scope(Constants.SCOPE_EMAIL),
            Scope(Constants.SCOPE_PROFILE)
        )
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    authorizationResult.pendingIntent?.let { pendingIntent ->
                        signInHandler?.onLaunchIntent(pendingIntent.intentSender)
                    }
                } else {
                    val token = authorizationResult.accessToken
                    if (token != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            fetchProfileAndSetSession(token)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun handleSignInResult(data: Any?) {
        val intent = (data as? Intent) ?: return
        try {
            val authorizationResult = Identity.getAuthorizationClient(context)
                .getAuthorizationResultFromIntent(intent)
            val token = authorizationResult.accessToken
            if (token != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    fetchProfileAndSetSession(token)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun signOut() {
        setSession(null, null, null)

        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        secureStorage.clear()
        signInHandler?.onSignOutRequested()
    }

    override fun setSession(token: String?, email: String?, name: String?) {
        _accessToken.value = token
        _userEmail.value = email
        _userName.value = name
        
        CoroutineScope(Dispatchers.IO).launch {
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
        return try {
            val requestedScopes = listOf(
                Scope(Constants.SCOPE_DRIVE_FILE),
                Scope(Constants.SCOPE_EMAIL),
                Scope(Constants.SCOPE_PROFILE)
            )
            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(requestedScopes)
                .build()

            val result = Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .await()

            val newToken = result.accessToken
            newToken?.let { setSession(it, _userEmail.value, _userName.value) }
            newToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
