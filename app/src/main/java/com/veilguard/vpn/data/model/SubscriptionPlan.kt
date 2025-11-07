package com.veilguard.vpn.data.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val duration_months: Int,
    val price: Double,
    val currency: String = "USD",
    val features: List<String> = emptyList()
)
