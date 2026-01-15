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
import java.net.InetAddress

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
        const val ACTION_STATUS_CHANGED = "com.veilguard.vpn.STATUS_CHANGED"
        const val EXTRA_CONNECTED = "connected"
        
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "VeilGuardVPN"
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
                val server = prefsManager.getSelectedServer()
                if (server != null) {
                    connect(server.ipAddress, 51820)
                } else {
                    Log.e(TAG, "No server selected")
                    stopSelf()
                }
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
            if (prepare(this) != null) {
                Log.e(TAG, "VPN permission not granted")
                return
            }
            
            // Build VPN interface
            val builder = Builder()
                .setSession("VeilGuard VPN")
                .addAddress("10.8.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setMtu(1420) // WireGuard standard MTU
                .setBlocking(true) // Use blocking for the tunnel thread
            
            // Establish VPN interface
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                return
            }
            
            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification("Connected to $serverIp"))
            broadcastStatus(true)
            
            // Create and start WireGuard tunnel
            wireGuardTunnel = WireGuardTunnel(vpnInterface!!, serverIp, serverPort)
            
            tunnelThread = Thread({
                try {
                    Log.i(TAG, "Starting WireGuard tunnel")
                    wireGuardTunnel?.start { channel ->
                        protect(channel.socket())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Tunnel error", e)
                    if (isRunning) disconnect()
                }
            }, "VpnTunnelThread")
            tunnelThread?.start()
            
            Log.i(TAG, "VPN connected successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting VPN", e)
            disconnect()
        }
    }
    
    private fun disconnect() {
        if (!isRunning && vpnInterface == null) return
        
        Log.i(TAG, "Disconnecting VPN")
        isRunning = false
        
        try {
            wireGuardTunnel?.stop()
            wireGuardTunnel = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tunnel", e)
        }
        
        try {
            tunnelThread?.interrupt()
            tunnelThread = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping thread", e)
        }
        
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interface", e)
        }
        
        broadcastStatus(false)
        stopForeground(true)
        stopSelf()
    }
    
    private fun broadcastStatus(connected: Boolean) {
        val intent = Intent(ACTION_STATUS_CHANGED)
        intent.putExtra(EXTRA_CONNECTED, connected)
        sendBroadcast(intent)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "VeilGuard VPN", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VeilGuard VPN")
            .setContentText(status)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }
}
