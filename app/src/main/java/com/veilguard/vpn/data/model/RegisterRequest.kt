package com.veilguard.vpn.data.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val device_id: String
)
