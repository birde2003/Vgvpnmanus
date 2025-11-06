package com.veilguard.vpn.data.model

data class VpnServer(
    val id: String,
    val name: String,
    val ip_address: String,
    val location: String,
    val status: String,
    val created_at: String
)
