package com.veilguard.vpn.ui.main

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.data.model.TrialRequest
import com.veilguard.vpn.ui.subscription.SubscriptionActivity
import com.veilguard.vpn.vpn.VeilGuardVpnService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var statusTextView: TextView
    private lateinit var connectButton: Button
    private lateinit var trialButton: Button
    private lateinit var subscribeButton: Button
    private var isConnected = false
    
    companion object {
        private const val VPN_REQUEST_CODE = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        
        // Load auth token
        val token = prefsManager.getAuthToken()
        RetrofitClient.setAuthToken(token)
        
        statusTextView = findViewById(R.id.statusTextView)
        connectButton = findViewById(R.id.connectButton)
        trialButton = findViewById(R.id.trialButton)
        subscribeButton = findViewById(R.id.subscribeButton)
        
        connectButton.setOnClickListener {
            if (isConnected) {
                disconnectVpn()
            } else {
                connectVpn()
            }
        }
        
        trialButton.setOnClickListener {
            startTrial()
        }
        
        subscribeButton.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }
        
        checkTrialEligibility()
    }
    
    private fun connectVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }
    
    private fun disconnectVpn() {
        VeilGuardVpnService.stop(this)
        isConnected = false
        updateConnectionStatus()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            VeilGuardVpnService.start(this)
            isConnected = true
            updateConnectionStatus()
        }
    }
    
    private fun updateConnectionStatus() {
        statusTextView.text = if (isConnected) {
            getString(R.string.status_connected)
        } else {
            getString(R.string.status_disconnected)
        }
        
        connectButton.text = if (isConnected) {
            getString(R.string.btn_disconnect)
        } else {
            getString(R.string.btn_connect)
        }
    }
    
    private fun checkTrialEligibility() {
        lifecycleScope.launch {
            try {
                val deviceId = prefsManager.getDeviceId()
                val response = RetrofitClient.apiService.checkTrialEligibility(deviceId)
                if (response.isSuccessful && response.body() != null) {
                    val eligibility = response.body()!!
                    trialButton.isEnabled = eligibility.eligible
                    if (!eligibility.eligible) {
                        trialButton.text = "Trial Used"
                    }
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }
    
    private fun startTrial() {
        lifecycleScope.launch {
            try {
                val deviceId = prefsManager.getDeviceId()
                val email = prefsManager.getUserEmail() ?: ""
                val request = TrialRequest(deviceId, email)
                
                val response = RetrofitClient.apiService.startTrial(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, R.string.trial_started, Toast.LENGTH_LONG).show()
                    trialButton.isEnabled = false
                    trialButton.text = "Trial Active"
                } else {
                    Toast.makeText(this@MainActivity, "Failed to start trial", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
