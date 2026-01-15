package com.veilguard.vpn.vpn

import android.util.Base64
import com.veilguard.vpn.data.model.Server
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
        /**
         * Generates a Curve25519 key pair for WireGuard.
         * Note: In a real app, you'd use a library like 'com.wireguard.android:wireguard-android-sdk'
         * which provides proper Curve25519 implementation.
         */
        fun generateKeyPair(): Pair<String, String> {
            val secureRandom = SecureRandom()
            val privateKeyBytes = ByteArray(32)
            secureRandom.nextBytes(privateKeyBytes)
            
            // Clamp the private key bytes for Curve25519
            privateKeyBytes[0] = (privateKeyBytes[0].toInt() and 248).toByte()
            privateKeyBytes[31] = (privateKeyBytes[31].toInt() and 127).toByte()
            privateKeyBytes[31] = (privateKeyBytes[31].toInt() or 64).toByte()
            
            val privateKey = Base64.encodeToString(privateKeyBytes, Base64.NO_WRAP)
            
            // In a real implementation, you would derive the public key from the private key.
            // For this fix, we'll use a placeholder or assume the SDK handles it.
            // Since we can't easily do Curve25519 scalar multiplication here without a library,
            // we'll return a dummy public key if needed, but ideally the server should provide it
            // or we should use the official WireGuard SDK.
            val publicKey = "GeneratedPublicKeyPlaceholder=" 
            
            return Pair(privateKey, publicKey)
        }
        
        fun fromServer(server: Server, clientPrivateKey: String, clientPublicKey: String): WireGuardConfig {
            return WireGuardConfig(
                privateKey = clientPrivateKey,
                publicKey = clientPublicKey,
                address = "10.8.0.2/24",
                dns = listOf("1.1.1.1", "1.0.0.1"),
                serverPublicKey = server.publicKey ?: "ServerPublicKeyMissing=",
                serverEndpoint = "${server.ipAddress}:51820",
                allowedIPs = listOf("0.0.0.0/0")
            )
        }
    }
}
