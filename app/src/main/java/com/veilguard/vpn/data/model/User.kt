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
