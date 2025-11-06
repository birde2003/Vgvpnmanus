package com.veilguard.vpn

import android.app.Application
import com.stripe.android.PaymentConfiguration

class VeilGuardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Stripe
        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )
    }
}
