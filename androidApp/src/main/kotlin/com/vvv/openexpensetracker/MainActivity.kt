package com.vvv.openexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.presentation.MainViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.setSignInHandler(object : GoogleAuthRepository.SignInHandler {
            override fun onSignInRequested() {
                // Now triggered via initiateSignIn in Repo
            }

            override fun onSignOutRequested() {
                // Handle any additional UI cleanup if needed
            }

            override fun onLaunchIntent(intentSender: Any) {
                val sender = intentSender as? android.content.IntentSender
                if (sender != null) {
                    try {
                        startIntentSenderForResult(
                            sender,
                            RC_AUTHORIZE,
                            null, 0, 0, 0
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })

        setContent {
            App()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_AUTHORIZE) {
            viewModel.handleSignInResult(data)
        }
    }

    companion object {
        private const val RC_AUTHORIZE = 1001
    }
}
