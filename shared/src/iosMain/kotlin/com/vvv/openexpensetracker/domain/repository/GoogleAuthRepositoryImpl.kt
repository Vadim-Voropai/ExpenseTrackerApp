package com.vvv.openexpensetracker.domain.repository

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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        signInHandler?.onSignInRequested()
    }

    override fun handleSignInResult(data: Any?) {
        // Not used on iOS currently as we simulate sign in from Swift
    }

    override fun signOut() {
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
        // Silent refresh on iOS would involve GIDSignIn.sharedInstance.restorePreviousSignIn
        // For now, since it's mocked, we just return the existing token
        return _accessToken.value
    }
}
