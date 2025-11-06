package com.veilguard.vpn.vpn

import android.util.Base64
import com.veilguard.vpn.data.model.Server
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom

data class WireGuardConfig(
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val dns: List<String>,
    val serverPublicKey: String,
    val serverEndpoint: String,
    val allowedIPs: List<String>,
    val persistentKeepalive: Int = 25
) {
    fun toConfigString(): String {
        return """
            [Interface]
            PrivateKey = $privateKey
            Address = $address
            DNS = ${dns.joinToString(", ")}
            
            [Peer]
            PublicKey = $serverPublicKey
            Endpoint = $serverEndpoint
            AllowedIPs = ${allowedIPs.joinToString(", ")}
            PersistentKeepalive = $persistentKeepalive
        """.trimIndent()
    }
    
    companion object {
        fun generateKeyPair(): Pair<String, String> {
            val keyPairGenerator = KeyPairGenerator.getInstance("X25519")
            keyPairGenerator.initialize(256, SecureRandom())
            val keyPair = keyPairGenerator.generateKeyPair()
            
            val privateKey = Base64.encodeToString(
                keyPair.private.encoded,
                Base64.NO_WRAP
            )
            val publicKey = Base64.encodeToString(
                keyPair.public.encoded,
                Base64.NO_WRAP
            )
            
            return Pair(privateKey, publicKey)
        }
        
        fun fromServer(server: Server, clientPrivateKey: String, clientPublicKey: String): WireGuardConfig {
            return WireGuardConfig(
                privateKey = clientPrivateKey,
                publicKey = clientPublicKey,
                address = "10.8.0.2/24",
                dns = listOf("1.1.1.1", "1.0.0.1"),
                serverPublicKey = server.publicKey ?: "",
                serverEndpoint = "${server.ipAddress}:51820",
                allowedIPs = listOf("0.0.0.0/0", "::/0")
            )
        }
    }
}
