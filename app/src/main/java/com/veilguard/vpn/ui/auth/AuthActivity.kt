package com.veilguard.vpn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.ui.main.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var prefsManager: PreferencesManager
    
    private var isLoginMode = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        
        prefsManager = PreferencesManager(this)
        
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }
        
        signupButton.setOnClickListener {
            performRegister()
        }
    }
    
    private fun performLogin() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@AuthActivity)
                val response = apiService.login(email, password)
                
                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    if (token != null) {
                        prefsManager.saveAuthToken(token)
                        prefsManager.saveUserEmail(email)
                        
                        Toast.makeText(this@AuthActivity, 
                            "Login successful!", Toast.LENGTH_SHORT).show()
                        
                        navigateToMain()
                    }
                } else {
                    Toast.makeText(this@AuthActivity, 
                        "Login failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, 
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun performRegister() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@AuthActivity)
                val deviceId = prefsManager.getDeviceId()
                val request = com.veilguard.vpn.data.model.RegisterRequest(
                    email = email,
                    password = password,
                    device_id = deviceId
                )
                
                val response = apiService.register(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@AuthActivity, 
                        "Registration successful! Logging in...", Toast.LENGTH_SHORT).show()
                    
                    // Auto-login after registration
                    performLogin()
                    
                    // Auto-start trial after registration
                    startTrialAfterSignup()
                } else {
                    Toast.makeText(this@AuthActivity, 
                        "Registration failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, 
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startTrialAfterSignup() {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken() ?: return@launch
                val email = prefsManager.getUserEmail() ?: return@launch
                val deviceId = prefsManager.getDeviceId()
                val apiService = RetrofitClient.getApiService(this@AuthActivity)
                val request = com.veilguard.vpn.data.model.TrialRequest(
                    email = email,
                    device_id = deviceId
                )
                apiService.startTrial(request)
            } catch (e: Exception) {
                // Ignore errors - user can manually start trial later
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
