package com.veilguard.vpn.ui.legal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.veilguard.vpn.R
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.onboarding.OnboardingActivity

class LegalActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var acceptCheckbox: CheckBox
    private lateinit var continueButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal)
        
        prefsManager = PreferencesManager(this)
        
        acceptCheckbox = findViewById(R.id.acceptCheckbox)
        continueButton = findViewById(R.id.continueButton)
        
        continueButton.setOnClickListener {
            if (acceptCheckbox.isChecked) {
                prefsManager.setTermsAccepted(true)
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, R.string.must_accept_terms, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
