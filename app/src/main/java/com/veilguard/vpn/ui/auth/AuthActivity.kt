package com.veilguard.vpn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.data.model.RegisterRequest
import com.veilguard.vpn.ui.main.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private var isLoginMode = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        
        prefsManager = PreferencesManager(this)
        
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        
        loginButton.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                isLoginMode = true
                updateUI()
            }
        }
        
        signupButton.setOnClickListener {
            if (!isLoginMode) {
                performSignup()
            } else {
                isLoginMode = false
                updateUI()
            }
        }
    }
    
    private fun updateUI() {
        if (isLoginMode) {
            loginButton.text = getString(R.string.btn_login)
            signupButton.text = getString(R.string.sign_up_here)
        } else {
            loginButton.text = getString(R.string.sign_in_here)
            signupButton.text = getString(R.string.btn_signup)
        }
    }
    
    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    prefsManager.saveAuthToken(authResponse.access_token)
                    prefsManager.saveUserEmail(email)
                    RetrofitClient.setAuthToken(authResponse.access_token)
                    
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@AuthActivity, R.string.error_login_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun performSignup() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 8) {
            Toast.makeText(this, R.string.error_short_password, Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val deviceId = prefsManager.getDeviceId()
                val request = RegisterRequest(email, password, deviceId)
                val response = RetrofitClient.apiService.register(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@AuthActivity, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                    isLoginMode = true
                    updateUI()
                } else {
                    Toast.makeText(this@AuthActivity, R.string.error_signup_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
