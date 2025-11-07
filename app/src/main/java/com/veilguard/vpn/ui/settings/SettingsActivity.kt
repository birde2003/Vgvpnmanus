package com.veilguard.vpn.ui.settings

import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.veilguard.vpn.R
import com.veilguard.vpn.data.local.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var autoConnectSwitch: Switch
    private lateinit var killSwitchSwitch: Switch
    private lateinit var darkModeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PreferencesManager(this)

        setupViews()
        loadSettings()
    }

    private fun setupViews() {
        autoConnectSwitch = findViewById(R.id.autoConnectSwitch)
        killSwitchSwitch = findViewById(R.id.killSwitchSwitch)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)

        autoConnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setAutoConnect(isChecked)
            Toast.makeText(this, 
                if (isChecked) "Auto-connect enabled" else "Auto-connect disabled",
                Toast.LENGTH_SHORT).show()
        }

        killSwitchSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setKillSwitch(isChecked)
            Toast.makeText(this,
                if (isChecked) "Kill switch enabled" else "Kill switch disabled",
                Toast.LENGTH_SHORT).show()
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setDarkMode(isChecked)
            applyDarkMode(isChecked)
        }
    }

    private fun loadSettings() {
        autoConnectSwitch.isChecked = prefsManager.isAutoConnectEnabled()
        killSwitchSwitch.isChecked = prefsManager.isKillSwitchEnabled()
        darkModeSwitch.isChecked = prefsManager.isDarkModeEnabled()
    }

    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }
}
