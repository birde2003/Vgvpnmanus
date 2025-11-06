package com.veilguard.vpn.vpn

import android.content.Context
import android.net.TrafficStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class ConnectionStats(
    val bytesReceived: Long = 0,
    val bytesSent: Long = 0,
    val connectionTime: Long = 0,
    val downloadSpeed: Long = 0,
    val uploadSpeed: Long = 0
) {
    fun getFormattedReceived(): String = formatBytes(bytesReceived)
    fun getFormattedSent(): String = formatBytes(bytesSent)
    fun getFormattedDownloadSpeed(): String = "${formatBytes(downloadSpeed)}/s"
    fun getFormattedUploadSpeed(): String = "${formatBytes(uploadSpeed)}/s"
    fun getFormattedConnectionTime(): String = formatDuration(connectionTime)
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}

class VpnStatisticsManager(private val context: Context) {
    private val _statistics = MutableStateFlow(ConnectionStats())
    val statistics: StateFlow<ConnectionStats> = _statistics.asStateFlow()
    
    private var startTime: Long = 0
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0
    private var lastUpdateTime: Long = 0
    
    fun startTracking() {
        startTime = System.currentTimeMillis()
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastUpdateTime = System.currentTimeMillis()
    }
    
    fun updateStatistics() {
        val currentTime = System.currentTimeMillis()
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        
        val timeDiff = (currentTime - lastUpdateTime) / 1000.0
        
        val downloadSpeed = if (timeDiff > 0) {
            ((currentRxBytes - lastRxBytes) / timeDiff).toLong()
        } else 0
        
        val uploadSpeed = if (timeDiff > 0) {
            ((currentTxBytes - lastTxBytes) / timeDiff).toLong()
        } else 0
        
        _statistics.value = ConnectionStats(
            bytesReceived = currentRxBytes - lastRxBytes,
            bytesSent = currentTxBytes - lastTxBytes,
            connectionTime = currentTime - startTime,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed
        )
        
        lastUpdateTime = currentTime
    }
    
    fun stopTracking() {
        startTime = 0
        lastRxBytes = 0
        lastTxBytes = 0
        lastUpdateTime = 0
        _statistics.value = ConnectionStats()
    }
}
