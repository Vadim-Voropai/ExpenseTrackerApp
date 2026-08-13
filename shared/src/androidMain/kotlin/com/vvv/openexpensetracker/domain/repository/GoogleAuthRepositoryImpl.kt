package com.vvv.openexpensetracker.domain.repository

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val httpClient: HttpClient
) : GoogleAuthRepository {
    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    override val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    override val userName: StateFlow<String?> = _userName.asStateFlow()

    private var signInHandler: GoogleAuthRepository.SignInHandler? = null

    override fun setSignInHandler(handler: GoogleAuthRepository.SignInHandler?) {
        this.signInHandler = handler
    }

    override fun initiateSignIn() {
        val requestedScopes = listOf(
            Scope("https://www.googleapis.com/auth/drive.file"),
            Scope("email"),
            Scope("profile")
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
        val intent = data as? Intent ?: return
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

    override fun signOut() {
        setSession(null, null, null)
        try {
            Identity.getSignInClient(context).signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        signInHandler?.onSignOutRequested()
    }

    override fun setSession(token: String?, email: String?, name: String?) {
        _accessToken.value = token
        _userEmail.value = email
        _userName.value = name
    }

    override fun isSignedIn(): Boolean {
        return _accessToken.value != null
    }

    override suspend fun fetchProfileAndSetSession(token: String) {
        try {
            val response = httpClient.get("https://www.googleapis.com/oauth2/v3/userinfo") {
                header("Authorization", "Bearer $token")
            }
            if (response.status.value == 200) {
                val body = response.bodyAsText()
                val jsonObject = Json.parseToJsonElement(body).jsonObject
                val email = jsonObject["email"]?.jsonPrimitive?.content
                val name = jsonObject["name"]?.jsonPrimitive?.content
                setSession(token, email, name)
            } else {
                setSession(token, "Authenticated", "Google User")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setSession(token, "Authenticated", "Google User")
        }
    }
}
