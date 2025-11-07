package com.veilguard.vpn.data.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val duration_months: Int,
    val price: Double,
    val currency: String = "USD",
    val features: List<String> = emptyList(),
    // Additional fields for UI display
    val duration: String = "$duration_months month${if (duration_months > 1) "s" else ""}",
    val priceId: String = "price_${duration_months}months"
)
