package com.veilguard.vpn.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return OnboardingFragment.newInstance(position)
    }
}
