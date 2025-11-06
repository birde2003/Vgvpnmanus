package com.veilguard.vpn.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Server(
    val id: String,
    val name: String,
    val ipAddress: String,
    val location: String,
    val status: String,
    val publicKey: String? = null,
    val createdAt: String? = null
) : Parcelable
