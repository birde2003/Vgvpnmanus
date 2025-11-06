package com.veilguard.vpn.data.model

data class Trial(
    val id: String,
    val device_id: String,
    val email: String,
    val status: String,
    val expires_at: String,
    val created_at: String
)

data class TrialRequest(
    val device_id: String,
    val email: String
)

data class TrialEligibility(
    val eligible: Boolean,
    val message: String
)
