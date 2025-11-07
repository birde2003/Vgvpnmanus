package com.veilguard.vpn.ui.splash

import android.animation.AnimatorSet
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
import com.veilguard.vpn.ui.auth.LoginActivity
import com.veilguard.vpn.ui.legal.LegalActivity
import com.veilguard.vpn.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var logo: ImageView
    private lateinit var appName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefsManager = PreferencesManager(this)
        
        logo = findViewById(R.id.splashLogo)
        appName = findViewById(R.id.appName)

        // Start animations
        animateSplash()

        // Navigate after delay
        lifecycleScope.launch {
            delay(2500) // 2.5 seconds
            navigateToNextScreen()
        }
    }

    private fun animateSplash() {
        // Logo fade in and scale animation
        val logoFadeIn = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f).apply {
            duration = 1000
        }
        
        val logoScaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.5f, 1f).apply {
            duration = 1000
        }
        
        val logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.5f, 1f).apply {
            duration = 1000
        }

        // App name slide up and fade in
        val appNameTranslate = ObjectAnimator.ofFloat(appName, View.TRANSLATION_Y, 100f, 0f).apply {
            duration = 800
            startDelay = 500
        }
        
        val appNameFadeIn = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f).apply {
            duration = 800
            startDelay = 500
        }

        // Play all animations together
        AnimatorSet().apply {
            playTogether(logoFadeIn, logoScaleX, logoScaleY, appNameTranslate, appNameFadeIn)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun navigateToNextScreen() {
        val intent = when {
            !prefsManager.hasAcceptedLegal() -> {
                Intent(this, LegalActivity::class.java)
            }
            prefsManager.getAuthToken() == null -> {
                Intent(this, LoginActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
        startActivity(intent)
        finish()
        
        // Add fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
