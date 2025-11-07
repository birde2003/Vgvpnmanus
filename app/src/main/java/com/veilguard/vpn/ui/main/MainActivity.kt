package com.veilguard.vpn.ui.main

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.servers.ServerSelectionActivity
import com.veilguard.vpn.ui.settings.SettingsActivity
import com.veilguard.vpn.ui.subscription.SubscriptionActivity
import com.veilguard.vpn.vpn.VeilGuardVpnService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var connectButton: Button
    private lateinit var selectServerButton: Button
    private lateinit var trialButton: Button
    private lateinit var subscribeButton: Button
    private lateinit var settingsButton: Button
    private lateinit var statusText: TextView
    private lateinit var selectedServerText: TextView
    private lateinit var statisticsCard: CardView
    
    private var isConnected = false
    private val VPN_REQUEST_CODE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        
        initViews()
        setupClickListeners()
        checkTrialStatus()
    }
    
    private fun initViews() {
        connectButton = findViewById(R.id.connectButton)
        selectServerButton = findViewById(R.id.selectServerButton)
        trialButton = findViewById(R.id.trialButton)
        subscribeButton = findViewById(R.id.subscribeButton)
        settingsButton = findViewById(R.id.settingsButton)
        statusText = findViewById(R.id.connectionStatusText)
        selectedServerText = findViewById(R.id.selectedServerText)
        statisticsCard = findViewById(R.id.statisticsCard)
    }
    
    private fun setupClickListeners() {
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
            activateTrial()
        }
        
        subscribeButton.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }
        
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun checkTrialStatus() {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken() ?: return@launch
                val apiService = RetrofitClient.apiService
                val deviceId = prefsManager.getDeviceId()
                val response = apiService.checkTrialEligibility(deviceId)
                
                if (response.isSuccessful) {
                    val eligibility = response.body()
                    if (eligibility?.eligible == false) {
                        trialButton.isEnabled = false
                        trialButton.text = "Trial Used"
                    }
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
    
    private fun activateTrial() {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken()
                if (token == null) {
                    Toast.makeText(this@MainActivity, 
                        "Please sign up first to start trial", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                val email = prefsManager.getUserEmail() ?: return@launch
                val deviceId = prefsManager.getDeviceId()
                val apiService = RetrofitClient.apiService
                val request = com.veilguard.vpn.data.model.TrialRequest(
                    email = email,
                    device_id = deviceId
                )
                val response = apiService.startTrial(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, 
                        "7-day free trial activated!", Toast.LENGTH_LONG).show()
                    trialButton.isEnabled = false
                    trialButton.text = "Trial Active"
                } else {
                    val error = response.errorBody()?.string() ?: "Failed to start trial"
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, 
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun connectVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            startVpnService()
        }
    }
    
    private fun startVpnService() {
        val selectedServer = prefsManager.getSelectedServer()
        if (selectedServer == null) {
            Toast.makeText(this, "Please select a server first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ServerSelectionActivity::class.java))
            return
        }
        
        val intent = Intent(this, VeilGuardVpnService::class.java)
        intent.action = VeilGuardVpnService.ACTION_CONNECT
        startService(intent)
        
        updateConnectionStatus(true)
    }
    
    private fun disconnectVpn() {
        val intent = Intent(this, VeilGuardVpnService::class.java)
        intent.action = VeilGuardVpnService.ACTION_DISCONNECT
        startService(intent)
        
        updateConnectionStatus(false)
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        isConnected = connected
        
        if (connected) {
            statusText.text = "Connected"
            statusText.setTextColor(getColor(R.color.green))
            connectButton.text = "Disconnect"
            connectButton.backgroundTintList = getColorStateList(R.color.red)
            statisticsCard.visibility = View.VISIBLE
            
            val server = prefsManager.getSelectedServer()
            selectedServerText.text = server?.name ?: "Unknown Server"
        } else {
            statusText.text = "Disconnected"
            statusText.setTextColor(getColor(R.color.red))
            connectButton.text = "Connect"
            connectButton.backgroundTintList = getColorStateList(R.color.primary)
            statisticsCard.visibility = View.GONE
            selectedServerText.text = "No server selected"
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startVpnService()
        }
    }
}
