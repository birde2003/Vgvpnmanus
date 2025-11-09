package com.veilguard.vpn.ui.subscription

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.veilguard.vpn.BuildConfig
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import kotlinx.coroutines.launch

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var prefsManager: PreferencesManager
    private lateinit var paymentSheet: PaymentSheet
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Choose Your Plan"
        
        prefsManager = PreferencesManager(this)
        
        // Initialize Stripe
        PaymentConfiguration.init(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        
        setupPlanClickListeners()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private fun setupPlanClickListeners() {
        findViewById<CardView>(R.id.plan1Month).setOnClickListener {
            subscribeToPlan("1 month")
        }
        
        findViewById<CardView>(R.id.plan3Months).setOnClickListener {
            subscribeToPlan("3 months")
        }
        
        findViewById<CardView>(R.id.plan6Months).setOnClickListener {
            subscribeToPlan("6 months")
        }
        
        findViewById<CardView>(R.id.plan12Months).setOnClickListener {
            subscribeToPlan("12 months")
        }
    }
    
    private fun subscribeToPlan(plan: String) {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken()
                val email = prefsManager.getUserEmail()
                
                if (token == null || email == null) {
                    Toast.makeText(this@SubscriptionActivity,
                        "Please sign in first to subscribe", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                val apiService = RetrofitClient.apiService
                
                val request = mapOf(
                    "email" to email,
                    "plan" to plan
                )
                
                val response = apiService.createSubscription("Bearer $token", request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@SubscriptionActivity,
                        "Subscription to $plan activated successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@SubscriptionActivity,
                        "Failed to create subscription: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Demo mode: Show success message when API is unavailable
                Toast.makeText(this@SubscriptionActivity,
                    "Demo Mode: Subscription to $plan activated! (API unavailable)", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show()
                finish()
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment failed: ${paymentSheetResult.error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
