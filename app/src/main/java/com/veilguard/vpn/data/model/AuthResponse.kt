package com.veilguard.vpn.data.model

data class AuthResponse(
    val access_token: String,
    val token_type: String = "bearer",
    val user: User? = null
)
