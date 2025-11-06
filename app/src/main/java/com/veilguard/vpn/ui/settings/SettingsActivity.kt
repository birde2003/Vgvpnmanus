package com.veilguard.vpn.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.veilguard.vpn.R
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.auth.AuthActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var autoConnectSwitch: Switch
    private lateinit var killSwitchSwitch: Switch
    private lateinit var darkModeSwitch: Switch
    private lateinit var userEmailText: TextView
    private lateinit var logoutButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        prefsManager = PreferencesManager(this)
        
        initializeViews()
        loadSettings()
        setupListeners()
    }
    
    private fun initializeViews() {
        autoConnectSwitch = findViewById(R.id.autoConnectSwitch)
        killSwitchSwitch = findViewById(R.id.killSwitchSwitch)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        userEmailText = findViewById(R.id.userEmailText)
        logoutButton = findViewById(R.id.logoutButton)
    }
    
    private fun loadSettings() {
        autoConnectSwitch.isChecked = prefsManager.isAutoConnectEnabled()
        killSwitchSwitch.isChecked = prefsManager.isKillSwitchEnabled()
        darkModeSwitch.isChecked = prefsManager.isDarkModeEnabled()
        userEmailText.text = prefsManager.getUserEmail() ?: "Not logged in"
    }
    
    private fun setupListeners() {
        autoConnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setAutoConnect(isChecked)
        }
        
        killSwitchSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setKillSwitch(isChecked)
        }
        
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setDarkMode(isChecked)
            applyTheme(isChecked)
        }
        
        logoutButton.setOnClickListener {
            logout()
        }
    }
    
    private fun applyTheme(darkMode: Boolean) {
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        recreate()
    }
    
    private fun logout() {
        prefsManager.clear()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
