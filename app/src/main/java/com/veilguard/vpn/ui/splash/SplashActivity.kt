package com.veilguard.vpn.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.auth.AuthActivity
import com.veilguard.vpn.ui.legal.LegalActivity
import com.veilguard.vpn.ui.main.MainActivity
import com.veilguard.vpn.ui.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000)
    }
    
    private fun navigateToNextScreen() {
        val intent = when {
            !prefsManager.areTermsAccepted() -> {
                Intent(this, LegalActivity::class.java)
            }
            !prefsManager.isOnboardingComplete() -> {
                Intent(this, OnboardingActivity::class.java)
            }
            prefsManager.getAuthToken() == null -> {
                Intent(this, AuthActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
        
        startActivity(intent)
        finish()
    }
}
