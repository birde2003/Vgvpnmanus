package com.veilguard.vpn.ui.subscription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.veilguard.vpn.R
import com.veilguard.vpn.data.model.SubscriptionPlan

class SubscriptionAdapter(
    private val plans: List<SubscriptionPlan>,
    private val onPlanClick: (SubscriptionPlan) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planName: TextView = view.findViewById(R.id.planName)
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
        holder.planPrice.text = "$${plan.price}"
        holder.subscribeButton.setOnClickListener {
            onPlanClick(plan)
        }
    }
    
    override fun getItemCount() = plans.size
}
