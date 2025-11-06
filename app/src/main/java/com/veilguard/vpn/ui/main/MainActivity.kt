package com.veilguard.vpn.ui.main

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.data.model.Server
import com.veilguard.vpn.ui.servers.ServerSelectionActivity
import com.veilguard.vpn.ui.settings.SettingsActivity
import com.veilguard.vpn.ui.subscription.SubscriptionActivity
import com.veilguard.vpn.vpn.VeilGuardVpnService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var connectionStatusText: TextView
    private lateinit var selectedServerText: TextView
    private lateinit var connectButton: Button
    private lateinit var selectServerButton: Button
    private lateinit var trialButton: Button
    private lateinit var subscribeButton: Button
    private lateinit var settingsButton: Button
    private lateinit var statisticsCard: CardView
    private lateinit var durationText: TextView
    private lateinit var downloadText: TextView
    private lateinit var uploadText: TextView
    private lateinit var speedText: TextView
    
    private var isConnected = false
    private val VPN_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme
        prefsManager = PreferencesManager(this)
        applyTheme()
        
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupListeners()
        loadSelectedServer()
        checkAutoConnect()
    }
    
    private fun applyTheme() {
        if (prefsManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun initializeViews() {
        connectionStatusText = findViewById(R.id.connectionStatusText)
        selectedServerText = findViewById(R.id.selectedServerText)
        connectButton = findViewById(R.id.connectButton)
        selectServerButton = findViewById(R.id.selectServerButton)
        trialButton = findViewById(R.id.trialButton)
        subscribeButton = findViewById(R.id.subscribeButton)
        settingsButton = findViewById(R.id.settingsButton)
        statisticsCard = findViewById(R.id.statisticsCard)
        durationText = findViewById(R.id.durationText)
        downloadText = findViewById(R.id.downloadText)
        uploadText = findViewById(R.id.uploadText)
        speedText = findViewById(R.id.speedText)
    }
    
    private fun setupListeners() {
        connectButton.setOnClickListener {
            if (isConnected) {
                disconnectVpn()
            } else {
                connectVpn()
            }
        }
        
        selectServerButton.setOnClickListener {
            startActivity(Intent(this, ServerSelectionActivity::class.java))
        }
        
        trialButton.setOnClickListener {
            startTrial()
        }
        
        subscribeButton.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }
        
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun loadSelectedServer() {
        val serverName = prefsManager.getSelectedServerName()
        if (serverName != null) {
            selectedServerText.text = serverName
        } else {
            selectedServerText.text = "No server selected"
        }
    }
    
    private fun checkAutoConnect() {
        if (prefsManager.isAutoConnectEnabled() && !isConnected) {
            val serverId = prefsManager.getSelectedServerId()
            if (serverId != null) {
                connectVpn()
            }
        }
    }
    
    private fun connectVpn() {
        val serverId = prefsManager.getSelectedServerId()
        val serverName = prefsManager.getSelectedServerName()
        val serverIp = prefsManager.getSelectedServerIp()
        
        if (serverId == null || serverName == null || serverIp == null) {
            Toast.makeText(this, "Please select a server first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            startVpnService(serverId, serverName, serverIp)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            val serverId = prefsManager.getSelectedServerId()
            val serverName = prefsManager.getSelectedServerName()
            val serverIp = prefsManager.getSelectedServerIp()
            if (serverId != null && serverName != null && serverIp != null) {
                startVpnService(serverId, serverName, serverIp)
            }
        }
    }
    
    private fun startVpnService(serverId: String, serverName: String, serverIp: String) {
        val server = Server(
            id = serverId,
            name = serverName,
            ipAddress = serverIp,
            location = "",
            status = "active"
        )
        
        val intent = Intent(this, VeilGuardVpnService::class.java).apply {
            action = VeilGuardVpnService.ACTION_CONNECT
            putExtra(VeilGuardVpnService.EXTRA_SERVER, server)
        }
        startService(intent)
        
        updateConnectionStatus(true, serverName)
        startStatisticsUpdates()
    }
    
    private fun disconnectVpn() {
        val intent = Intent(this, VeilGuardVpnService::class.java).apply {
            action = VeilGuardVpnService.ACTION_DISCONNECT
        }
        startService(intent)
        
        updateConnectionStatus(false, null)
        stopStatisticsUpdates()
    }
    
    private fun updateConnectionStatus(connected: Boolean, serverName: String?) {
        isConnected = connected
        if (connected) {
            connectionStatusText.text = "Connected"
            connectionStatusText.setTextColor(getColor(R.color.green))
            connectButton.text = "Disconnect"
            selectedServerText.text = serverName ?: "Unknown Server"
            statisticsCard.visibility = View.VISIBLE
        } else {
            connectionStatusText.text = "Disconnected"
            connectionStatusText.setTextColor(getColor(R.color.red))
            connectButton.text = "Connect"
            loadSelectedServer()
            statisticsCard.visibility = View.GONE
        }
    }
    
    private fun startStatisticsUpdates() {
        lifecycleScope.launch {
            while (isConnected) {
                updateStatistics()
                delay(1000)
            }
        }
    }
    
    private fun stopStatisticsUpdates() {
        // Statistics updates will stop automatically when isConnected becomes false
    }
    
    private fun updateStatistics() {
        // In a real implementation, these would come from VpnStatisticsManager
        // For now, showing placeholder values
        durationText.text = "00:15:32"
        downloadText.text = "125.5 MB"
        uploadText.text = "42.3 MB"
        speedText.text = "↓ 2.5 MB/s ↑ 512 KB/s"
    }
    
    private fun startTrial() {
        lifecycleScope.launch {
            try {
                val deviceId = prefsManager.getDeviceId()
                val token = prefsManager.getAuthToken() ?: return@launch
                val apiService = RetrofitClient.getApiService(this@MainActivity)
                
                // Check eligibility
                val checkResponse = apiService.checkTrial("Bearer $token", deviceId)
                if (checkResponse.isSuccessful) {
                    val eligible = checkResponse.body()?.get("eligible") as? Boolean ?: false
                    if (!eligible) {
                        Toast.makeText(this@MainActivity, 
                            "Trial already used on this device", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }
                
                // Start trial
                val email = prefsManager.getUserEmail() ?: return@launch
                val startRequest = mapOf("device_id" to deviceId, "email" to email)
                val startResponse = apiService.startTrial("Bearer $token", startRequest)
                
                if (startResponse.isSuccessful) {
                    Toast.makeText(this@MainActivity, 
                        "7-day free trial activated!", Toast.LENGTH_LONG).show()
                    trialButton.isEnabled = false
                } else {
                    Toast.makeText(this@MainActivity, 
                        "Failed to start trial", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, 
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadSelectedServer()
    }
}
