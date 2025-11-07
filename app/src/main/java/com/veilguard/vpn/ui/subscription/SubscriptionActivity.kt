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
        
        prefsManager = PreferencesManager(this)
        
        // Initialize Stripe
        PaymentConfiguration.init(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        
        setupPlanClickListeners()
    }
    
    private fun setupPlanClickListeners() {
        findViewById<CardView>(R.id.plan1Month).setOnClickListener {
            subscribeToPlan("1 month", 9.99)
        }
        
        findViewById<CardView>(R.id.plan3Months).setOnClickListener {
            subscribeToPlan("3 months", 24.99)
        }
        
        findViewById<CardView>(R.id.plan6Months).setOnClickListener {
            subscribeToPlan("6 months", 44.99)
        }
        
        findViewById<CardView>(R.id.plan12Months).setOnClickListener {
            subscribeToPlan("12 months", 79.99)
        }
    }
    
    private fun subscribeToPlan(plan: String, price: Double) {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken() ?: return@launch
                val email = prefsManager.getUserEmail() ?: return@launch
                val apiService = RetrofitClient.apiService
                
                val request = mapOf(
                    "email" to email,
                    "plan" to plan
                )
                
                val response = apiService.createSubscription("Bearer $token", request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@SubscriptionActivity,
                        "Subscription created successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@SubscriptionActivity,
                        "Failed to create subscription", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SubscriptionActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
