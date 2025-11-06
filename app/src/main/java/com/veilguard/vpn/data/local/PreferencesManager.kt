package com.veilguard.vpn.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.veilguard.vpn.utils.Constants

class PreferencesManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveAuthToken(token: String) {
        prefs.edit().putString(Constants.KEY_AUTH_TOKEN, token).apply()
    }
    
    fun getAuthToken(): String? {
        return prefs.getString(Constants.KEY_AUTH_TOKEN, null)
    }
    
    fun saveUserEmail(email: String) {
        prefs.edit().putString(Constants.KEY_USER_EMAIL, email).apply()
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(Constants.KEY_USER_EMAIL, null)
    }
    
    fun getDeviceId(): String {
        var deviceId = prefs.getString(Constants.KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(Constants.KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
    
    fun setOnboardingComplete(complete: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_ONBOARDING_COMPLETE, complete).apply()
    }
    
    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean(Constants.KEY_ONBOARDING_COMPLETE, false)
    }
    
    fun setTermsAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_TERMS_ACCEPTED, accepted).apply()
    }
    
    fun areTermsAccepted(): Boolean {
        return prefs.getBoolean(Constants.KEY_TERMS_ACCEPTED, false)
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
