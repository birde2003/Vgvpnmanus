package com.veilguard.vpn.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.veilguard.vpn.R
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.data.model.Server
import com.veilguard.vpn.ui.main.MainActivity
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class VeilGuardVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var statisticsManager: VpnStatisticsManager? = null
    private var currentServer: Server? = null
    private var wireGuardConfig: WireGuardConfig? = null
    
    companion object {
        const val ACTION_CONNECT = "com.veilguard.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.veilguard.vpn.DISCONNECT"
        const val EXTRA_SERVER = "extra_server"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "vpn_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        statisticsManager = VpnStatisticsManager(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val server = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_SERVER, Server::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_SERVER)
                }
                server?.let { connectVpn(it) }
            }
            ACTION_DISCONNECT -> disconnectVpn()
        }
        return START_STICKY
    }
    
    private fun connectVpn(server: Server) {
        if (isRunning) return
        
        currentServer = server
        
        // Generate WireGuard keys
        val (privateKey, publicKey) = WireGuardConfig.generateKeyPair()
        wireGuardConfig = WireGuardConfig.fromServer(server, privateKey, publicKey)
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification("Connecting...", server.name))
        
        serviceScope.launch {
            try {
                establishVpnTunnel(server)
                isRunning = true
                statisticsManager?.startTracking()
                
                // Update notification
                val notification = createNotification("Connected", server.name)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, notification)
                
                // Start statistics updates
                startStatisticsUpdates()
                
                // Handle VPN traffic
                handleVpnTraffic()
            } catch (e: Exception) {
                e.printStackTrace()
                disconnectVpn()
            }
        }
    }
    
    private fun establishVpnTunnel(server: Server) {
        val builder = Builder()
            .setSession("VeilGuard VPN")
            .addAddress("10.8.0.2", 24)
            .addDnsServer("1.1.1.1")
            .addDnsServer("1.0.0.1")
            .addRoute("0.0.0.0", 0)
            .setMtu(1420)
        
        // Apply kill switch if enabled
        val prefsManager = PreferencesManager(this)
        if (prefsManager.isKillSwitchEnabled()) {
            builder.setBlocking(true)
        }
        
        vpnInterface = builder.establish()
    }
    
    private fun handleVpnTraffic() {
        serviceScope.launch {
            val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
            val buffer = ByteBuffer.allocate(32767)
            
            try {
                while (isRunning && vpnInterface != null) {
                    val length = vpnInput.read(buffer.array())
                    if (length > 0) {
                        // Process packet through WireGuard
                        buffer.limit(length)
                        processPacket(buffer)
                        buffer.clear()
                    }
                    delay(10)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun processPacket(buffer: ByteBuffer) {
        // WireGuard packet processing
        // In production, this would use the WireGuard native library
        // For now, this is a placeholder for the actual implementation
    }
    
    private fun startStatisticsUpdates() {
        serviceScope.launch {
            while (isRunning) {
                statisticsManager?.updateStatistics()
                delay(1000)
            }
        }
    }
    
    private fun disconnectVpn() {
        isRunning = false
        statisticsManager?.stopTracking()
        
        vpnInterface?.close()
        vpnInterface = null
        
        currentServer = null
        wireGuardConfig = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VeilGuard VPN connection status"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String, serverName: String) = 
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VeilGuard VPN")
            .setContentText("$status - $serverName")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    
    override fun onDestroy() {
        disconnectVpn()
        serviceScope.cancel()
        super.onDestroy()
    }
}
