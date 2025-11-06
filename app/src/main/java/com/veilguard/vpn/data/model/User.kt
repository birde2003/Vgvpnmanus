package com.veilguard.vpn.data.model

data class User(
    val id: String,
    val email: String,
    val deviceId: String,
    val createdAt: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val device_id: String
)

data class AuthResponse(
    val access_token: String,
    val token_type: String
)
