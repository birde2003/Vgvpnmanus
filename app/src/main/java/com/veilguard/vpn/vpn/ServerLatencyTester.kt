package com.veilguard.vpn.vpn

import com.veilguard.vpn.data.model.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class ServerLatencyTester {
    suspend fun testLatency(server: Server): Long = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(server.ipAddress, 51820), 5000)
            val latency = System.currentTimeMillis() - startTime
            socket.close()
            latency
        } catch (e: Exception) {
            -1L // Indicates server is unreachable
        }
    }
    
    suspend fun testMultipleServers(servers: List<Server>): Map<String, Long> {
        return servers.associate { server ->
            server.id to testLatency(server)
        }
    }
}
