package com.veilguard.vpn.data.model

data class TrialEligibility(
    val eligible: Boolean,
    val message: String,
    val device_id: String
)
