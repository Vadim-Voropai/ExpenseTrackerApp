package com.vadim.expensetracker.domain.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface GoogleAuthRepository {
    val accessToken: StateFlow<String?>
    val userEmail: StateFlow<String?>
    val userName: StateFlow<String?>

    fun initiateSignIn()
    fun signOut()
    fun setSession(token: String?, email: String?, name: String?)
    fun isSignedIn(): Boolean
    suspend fun fetchProfileAndSetSession(token: String)

    interface SignInHandler {
        fun onSignInRequested()
        fun onSignOutRequested()
    }
}

class GoogleAuthRepositoryImpl(
    private val httpClient: HttpClient
) : GoogleAuthRepository {
    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    override val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    override val userName: StateFlow<String?> = _userName.asStateFlow()

    var signInHandler: GoogleAuthRepository.SignInHandler? = null

    override fun initiateSignIn() {
        signInHandler?.onSignInRequested()
    }

    override fun signOut() {
        setSession(null, null, null)
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
