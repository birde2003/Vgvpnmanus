package com.veilguard.vpn.ui.splash

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.veilguard.vpn.R
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.auth.AuthActivity
import com.veilguard.vpn.ui.legal.LegalActivity
import com.veilguard.vpn.ui.main.MainActivity
import com.veilguard.vpn.ui.onboarding.OnboardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var logoImage: ImageView
    private lateinit var appNameText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        prefsManager = PreferencesManager(this)
        
        logoImage = findViewById(R.id.splashLogo)
        appNameText = findViewById(R.id.appNameText)
        
        // Start animations
        animateLogo()
        animateAppName()
        
        // Navigate after delay
        lifecycleScope.launch {
            delay(2500)
            navigateToNextScreen()
        }
    }
    
    private fun animateLogo() {
        // Scale animation
        val scaleX = ObjectAnimator.ofFloat(logoImage, View.SCALE_X, 0.5f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(logoImage, View.SCALE_Y, 0.5f, 1.2f, 1.0f)
        scaleX.duration = 1500
        scaleY.duration = 1500
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()
        scaleX.start()
        scaleY.start()
        
        // Fade in
        val alpha = ObjectAnimator.ofFloat(logoImage, View.ALPHA, 0f, 1f)
        alpha.duration = 1000
        alpha.start()
    }
    
    private fun animateAppName() {
        // Slide up and fade in
        appNameText.translationY = 50f
        appNameText.alpha = 0f
        
        val slideUp = ObjectAnimator.ofFloat(appNameText, View.TRANSLATION_Y, 50f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(appNameText, View.ALPHA, 0f, 1f)
        
        slideUp.duration = 1000
        fadeIn.duration = 1000
        slideUp.startDelay = 500
        fadeIn.startDelay = 500
        
        slideUp.start()
        fadeIn.start()
    }
    
    private fun navigateToNextScreen() {
        val intent = when {
            !prefsManager.isTermsAccepted() -> Intent(this, LegalActivity::class.java)
            !prefsManager.isOnboardingCompleted() -> Intent(this, OnboardingActivity::class.java)
            prefsManager.getAuthToken() == null -> Intent(this, AuthActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
