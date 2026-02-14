package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View
    
    private val layouts = listOf(
        R.layout.activity_onboarding,
        R.layout.activity_onboarding_page2,
        R.layout.activity_onboarding_page3
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_container)
        
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNextMain)
        tvSkip = findViewById(R.id.tvSkipMain)
        indicator1 = findViewById(R.id.indicatorMain1)
        indicator2 = findViewById(R.id.indicatorMain2)
        indicator3 = findViewById(R.id.indicatorMain3)
        
        setupViewPager()
        setupClickListeners()
        updateIndicators(0)
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingPagerAdapter(layouts)
        viewPager.adapter = adapter
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
                
                // Update button text on last page
                if (position == layouts.size - 1) {
                    btnNext.text = getString(R.string.get_started)
                } else {
                    btnNext.text = getString(R.string.next)
                }
            }
        })
    }
    
    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            if (viewPager.currentItem < layouts.size - 1) {
                viewPager.currentItem += 1
            } else {
                // Navigate to login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        
        tvSkip.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateIndicators(position: Int) {
        when (position) {
            0 -> {
                indicator1.setBackgroundResource(R.drawable.indicator_active)
                indicator2.setBackgroundResource(R.drawable.indicator_inactive)
                indicator3.setBackgroundResource(R.drawable.indicator_inactive)
            }
            1 -> {
                indicator1.setBackgroundResource(R.drawable.indicator_inactive)
                indicator2.setBackgroundResource(R.drawable.indicator_active)
                indicator3.setBackgroundResource(R.drawable.indicator_inactive)
            }
            2 -> {
                indicator1.setBackgroundResource(R.drawable.indicator_inactive)
                indicator2.setBackgroundResource(R.drawable.indicator_inactive)
                indicator3.setBackgroundResource(R.drawable.indicator_active)
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
