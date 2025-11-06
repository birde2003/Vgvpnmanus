package com.veilguard.vpn.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.veilguard.vpn.R

class OnboardingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val position = arguments?.getInt(ARG_POSITION) ?: 0
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val descTextView = view.findViewById<TextView>(R.id.descTextView)
        
        when (position) {
            0 -> {
                titleTextView.text = getString(R.string.onboarding_title_1)
                descTextView.text = getString(R.string.onboarding_desc_1)
            }
            1 -> {
                titleTextView.text = getString(R.string.onboarding_title_2)
                descTextView.text = getString(R.string.onboarding_desc_2)
            }
            2 -> {
                titleTextView.text = getString(R.string.onboarding_title_3)
                descTextView.text = getString(R.string.onboarding_desc_3)
            }
        }
    }
    
    companion object {
        private const val ARG_POSITION = "position"
        
        fun newInstance(position: Int): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }
}
