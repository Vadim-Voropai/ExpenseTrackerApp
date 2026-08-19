package com.vvv.openexpensetracker.data.model.remote

import kotlinx.serialization.Serializable

@Serializable
data class GoogleUserInfo(
    val email: String? = null,
    val name: String? = null
)
