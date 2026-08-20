package com.vvv.openexpensetracker.presentation

sealed interface MainIntent {
    data class HandleSignInResult(val data: Any?) : MainIntent
}
