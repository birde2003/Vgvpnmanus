package com.veilguard.vpn.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.veilguard.vpn.BuildConfig
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import kotlinx.coroutines.launch

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val duration: String,
    val price: String,
    val priceId: String
)

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubscriptionAdapter
    private lateinit var prefsManager: PreferencesManager
    private lateinit var paymentSheet: PaymentSheet
    
    private val plans = listOf(
        SubscriptionPlan("1", "1 Month", "1 month", "$9.99", "price_1month"),
        SubscriptionPlan("3", "3 Months", "3 months", "$24.99", "price_3months"),
        SubscriptionPlan("6", "6 Months", "6 months", "$44.99", "price_6months"),
        SubscriptionPlan("12", "12 Months", "12 months", "$79.99", "price_12months")
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        
        prefsManager = PreferencesManager(this)
        
        // Initialize Stripe
        PaymentConfiguration.init(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        
        setupRecyclerView()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.subscriptionRecyclerView)
        adapter = SubscriptionAdapter(plans) { plan ->
            subscribeToPlan(plan)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun subscribeToPlan(plan: SubscriptionPlan) {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken() ?: return@launch
                val email = prefsManager.getUserEmail() ?: return@launch
                val apiService = RetrofitClient.getApiService(this@SubscriptionActivity)
                
                val request = mapOf(
                    "email" to email,
                    "plan" to plan.duration
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

class SubscriptionAdapter(
    private val plans: List<SubscriptionPlan>,
    private val onPlanClick: (SubscriptionPlan) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planName: TextView = view.findViewById(R.id.planName)
        val planDuration: TextView = view.findViewById(R.id.planDuration)
        val planPrice: TextView = view.findViewById(R.id.planPrice)
        val subscribeButton: Button = view.findViewById(R.id.subscribeButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription_plan, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = plans[position]
        holder.planName.text = plan.name
        holder.planDuration.text = plan.duration
        holder.planPrice.text = plan.price
        holder.subscribeButton.setOnClickListener {
            onPlanClick(plan)
        }
    }
    
    override fun getItemCount() = plans.size
}
