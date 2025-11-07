package com.veilguard.vpn.data.model

data class Trial(
    val id: String,
    val device_id: String,
    val email: String,
    val status: String,
    val expires_at: String,
    val created_at: String
)
