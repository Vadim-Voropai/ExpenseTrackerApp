package com.vvv.openexpensetracker.domain.repository

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.vvv.openexpensetracker.core.Constants
import com.vvv.openexpensetracker.data.source.local.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GoogleAuthRepositoryImpl(
    private val context: Context,
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

    private fun getBaseUrl(): String = ""

    private suspend inline fun <reified T> get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = httpClient.get("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = httpClient.post("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> put(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = httpClient.put("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> patch(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = httpClient.patch("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> delete(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = httpClient.delete("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ClientRequestException(response, "")
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
        CoroutineScope(Dispatchers.Main).launch {
            setSession(null, null, null)
        }
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
            val body: String = get(Constants.GOOGLE_USERINFO_URL) {
                header("Authorization", "Bearer $token")
            }
            val jsonObject = Json.parseToJsonElement(body).jsonObject
            val email = jsonObject["email"]?.jsonPrimitive?.content
            val name = jsonObject["name"]?.jsonPrimitive?.content
            setSession(token, email, name)
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
            if (newToken != null) {
                setSession(newToken, _userEmail.value, _userName.value)
            }
            newToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
