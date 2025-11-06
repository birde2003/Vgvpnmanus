package com.veilguard.vpn.data.model

data class Subscription(
    val id: String,
    val email: String,
    val plan: String,
    val status: String,
    val expires_at: String,
    val stripe_subscription_id: String?
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val duration_months: Int,
    val price: Double,
    val currency: String
)
