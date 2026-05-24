package com.vadim.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.vadim.expensetracker.domain.repository.GoogleAuthRepository
import com.vadim.expensetracker.domain.repository.GoogleAuthRepositoryImpl
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val googleAuthRepository: GoogleAuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authImpl = googleAuthRepository as? GoogleAuthRepositoryImpl
        if (authImpl != null) {
            authImpl.signInHandler = object : GoogleAuthRepository.SignInHandler {
                override fun onSignInRequested() {
                    launchGoogleSignIn()
                }

                override fun onSignOutRequested() {
                    try {
                        Identity.getSignInClient(this@MainActivity).signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        setContent {
            App()
        }
    }

    private fun launchGoogleSignIn() {
        val requestedScopes = listOf(
            Scope("https://www.googleapis.com/auth/drive.file"),
            Scope("email"),
            Scope("profile")
        )
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    authorizationResult.pendingIntent?.let { pendingIntent ->
                        try {
                            startIntentSenderForResult(
                                pendingIntent.intentSender,
                                RC_AUTHORIZE,
                                null, 0, 0, 0
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val token = authorizationResult.accessToken
                    fetchAndSetSession(token)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_AUTHORIZE) {
            try {
                val authorizationResult = Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(data)
                val token = authorizationResult.accessToken
                fetchAndSetSession(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchAndSetSession(token: String?) {
        if (token == null) return
        lifecycleScope.launch {
            googleAuthRepository.fetchProfileAndSetSession(token)
        }
    }

    companion object {
        private const val RC_AUTHORIZE = 1001
    }
}
