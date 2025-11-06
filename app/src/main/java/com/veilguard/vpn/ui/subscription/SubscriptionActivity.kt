package com.veilguard.vpn.ui.subscription

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.model.SubscriptionPlan
import kotlinx.coroutines.launch

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubscriptionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        
        recyclerView = findViewById(R.id.plansRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadSubscriptionPlans()
    }
    
    private fun loadSubscriptionPlans() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getSubscriptionPlans()
                if (response.isSuccessful && response.body() != null) {
                    val plans = response.body()!!
                    adapter = SubscriptionAdapter(plans) { plan ->
                        onPlanSelected(plan)
                    }
                    recyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@SubscriptionActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun onPlanSelected(plan: SubscriptionPlan) {
        Toast.makeText(this, "Selected: ${plan.name}", Toast.LENGTH_SHORT).show()
        // TODO: Implement Stripe payment flow
    }
}
