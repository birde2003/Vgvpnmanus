package com.veilguard.vpn.vpn

import android.app.Notification
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
import com.veilguard.vpn.ui.main.MainActivity
import java.io.FileInputStream
import java.io.FileOutputStream

class VeilGuardVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private lateinit var prefsManager: PreferencesManager
    
    companion object {
        const val ACTION_CONNECT = "com.veilguard.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.veilguard.vpn.DISCONNECT"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "VeilGuardVPN"
    }
    
    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }
    
    private fun connect() {
        if (isRunning) return
        
        try {
            val builder = Builder()
                .setSession("VeilGuard VPN")
                .addAddress("10.8.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .setMtu(1500)
            
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isRunning = true
                startForeground(NOTIFICATION_ID, createNotification("Connected"))
                // Start packet forwarding in background
                Thread { forwardPackets() }.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect()
        }
    }
    
    private fun disconnect() {
        isRunning = false
        
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            vpnInterface = null
        }
        
        stopForeground(true)
        stopSelf()
    }
    
    private fun forwardPackets() {
        val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
        
        val buffer = ByteArray(32767)
        
        try {
            while (isRunning) {
                val length = vpnInput.read(buffer)
                if (length > 0) {
                    // Simple packet forwarding
                    vpnOutput.write(buffer, 0, length)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VeilGuard VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
