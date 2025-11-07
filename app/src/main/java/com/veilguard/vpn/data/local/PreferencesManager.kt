package com.veilguard.vpn.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "veilguard_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }
    
    fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }
    
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
    }
    
    fun getUserEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }
    
    fun getDeviceId(): String {
        var deviceId = sharedPreferences.getString("device_id", null)
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            sharedPreferences.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }
    
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean("onboarding_completed", completed).apply()
    }
    
    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean("onboarding_completed", false)
    }
    
    fun setTermsAccepted(accepted: Boolean) {
        sharedPreferences.edit().putBoolean("terms_accepted", accepted).apply()
    }
    
    fun isTermsAccepted(): Boolean {
        return sharedPreferences.getBoolean("terms_accepted", false)
    }
    
    fun setSelectedServer(serverId: String, serverName: String, serverIp: String) {
        sharedPreferences.edit()
            .putString("selected_server_id", serverId)
            .putString("selected_server_name", serverName)
            .putString("selected_server_ip", serverIp)
            .apply()
    }
    
    fun getSelectedServerId(): String? {
        return sharedPreferences.getString("selected_server_id", null)
    }
    
    fun getSelectedServerName(): String? {
        return sharedPreferences.getString("selected_server_name", null)
    }
    
    fun getSelectedServerIp(): String? {
        return sharedPreferences.getString("selected_server_ip", null)
    }
    
    // Settings
    fun setAutoConnect(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_connect", enabled).apply()
    }
    
    fun isAutoConnectEnabled(): Boolean {
        return sharedPreferences.getBoolean("auto_connect", false)
    }
    
    fun setKillSwitch(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("kill_switch", enabled).apply()
    }
    
    fun isKillSwitchEnabled(): Boolean {
        return sharedPreferences.getBoolean("kill_switch", false)
    }
    
    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }
    
    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }
    
    fun setOnboardingComplete(complete: Boolean) {
        sharedPreferences.edit().putBoolean("onboarding_completed", complete).apply()
    }
    
    fun hasAcceptedLegal(): Boolean {
        return sharedPreferences.getBoolean("terms_accepted", false)
    }
    
    fun getSelectedServer(): com.veilguard.vpn.data.model.Server? {
        val id = sharedPreferences.getString("selected_server_id", null) ?: return null
        val name = sharedPreferences.getString("selected_server_name", null) ?: return null
        val ip = sharedPreferences.getString("selected_server_ip", null) ?: return null
        return com.veilguard.vpn.data.model.Server(
            id = id,
            name = name,
            ipAddress = ip,
            location = "",
            status = "active",
            publicKey = null,
            createdAt = null
        )
    }
    
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
