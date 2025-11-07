package com.veilguard.vpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.veilguard.vpn.R
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.main.MainActivity
import java.nio.channels.DatagramChannel

class VeilGuardVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private lateinit var prefsManager: PreferencesManager
    private var tunnelThread: Thread? = null
    private var wireGuardTunnel: WireGuardTunnel? = null
    
    companion object {
        private const val TAG = "VeilGuardVpnService"
        const val ACTION_CONNECT = "com.veilguard.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.veilguard.vpn.DISCONNECT"
        const val EXTRA_SERVER_IP = "server_ip"
        const val EXTRA_SERVER_PORT = "server_port"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "VeilGuardVPN"
        
        // Default VPN server (can be overridden)
        private const val DEFAULT_SERVER_IP = "134.122.76.222"
        private const val DEFAULT_SERVER_PORT = 51820
    }
    
    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        createNotificationChannel()
        Log.d(TAG, "VPN Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val serverIp = intent.getStringExtra(EXTRA_SERVER_IP) ?: DEFAULT_SERVER_IP
                val serverPort = intent.getIntExtra(EXTRA_SERVER_PORT, DEFAULT_SERVER_PORT)
                connect(serverIp, serverPort)
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }
    
    private fun connect(serverIp: String, serverPort: Int) {
        if (isRunning) {
            Log.w(TAG, "VPN already running")
            return
        }
        
        Log.i(TAG, "Connecting to VPN server: $serverIp:$serverPort")
        
        try {
            // Check VPN permission
            val intent = prepare(this)
            if (intent != null) {
                Log.e(TAG, "VPN permission not granted")
                return
            }
            
            // Build VPN interface
            val builder = Builder()
                .setSession("VeilGuard VPN")
                .addAddress("10.8.0.2", 24)  // Client IP in VPN network
                .addRoute("0.0.0.0", 0)       // Route all traffic through VPN
                .addDnsServer("8.8.8.8")      // Google DNS
                .addDnsServer("8.8.4.4")      // Google DNS backup
                .setMtu(1500)                 // Standard MTU
                .setBlocking(false)           // Non-blocking mode
            
            // Allow apps to bypass VPN if needed
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    builder.setMetered(false)  // Mark as unmetered
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set metered status", e)
            }
            
            // Establish VPN interface
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                disconnect()
                return
            }
            
            Log.i(TAG, "VPN interface established")
            isRunning = true
            
            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification("Connected to $serverIp"))
            
            // Create and start WireGuard tunnel
            wireGuardTunnel = WireGuardTunnel(vpnInterface!!, serverIp, serverPort)
            
            tunnelThread = Thread {
                try {
                    Log.i(TAG, "Starting WireGuard tunnel")
                    wireGuardTunnel?.start { channel ->
                        // Protect socket from being routed through VPN
                        protect(channel.socket())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Tunnel error", e)
                    disconnect()
                }
            }
            tunnelThread?.start()
            
            Log.i(TAG, "VPN connected successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting VPN", e)
            disconnect()
        }
    }
    
    private fun disconnect() {
        Log.i(TAG, "Disconnecting VPN")
        
        // Set running flag to false first
        isRunning = false
        
        // Stop WireGuard tunnel
        try {
            wireGuardTunnel?.stop()
            wireGuardTunnel = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tunnel", e)
        }
        
        // Wait for tunnel thread to finish
        try {
            tunnelThread?.interrupt()
            tunnelThread?.join(2000) // Wait max 2 seconds
            tunnelThread = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tunnel thread", e)
        }
        
        // Close VPN interface - CRITICAL for releasing VPN key
        try {
            vpnInterface?.close()
            vpnInterface = null
            Log.i(TAG, "VPN interface closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        
        // Stop foreground service
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground", e)
        }
        
        // Stop the service
        stopSelf()
        
        Log.i(TAG, "VPN disconnected")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VeilGuard VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows VPN connection status"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Add disconnect action
        val disconnectIntent = Intent(this, VeilGuardVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VeilGuard VPN")
            .setContentText(status)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "Disconnect", disconnectPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onRevoke() {
        // Called when VPN permission is revoked by user
        Log.w(TAG, "VPN permission revoked")
        disconnect()
        super.onRevoke()
    }
    
    override fun onDestroy() {
        Log.i(TAG, "VPN Service destroyed")
        disconnect()
        super.onDestroy()
    }
}
