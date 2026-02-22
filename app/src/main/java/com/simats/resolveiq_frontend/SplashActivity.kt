package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.utils.UserPreferences

class SplashActivity : AppCompatActivity() {
    
    private val SPLASH_DURATION = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Animate the logo
        val logo = findViewById<ImageView>(R.id.ivLogo)
        val scaleAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        logo.startAnimation(scaleAnimation)
        
        // Animate progress bar
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        animateProgressBar(progressBar)
        
        // Navigate based on token presence after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val userPreferences = UserPreferences(this)
            val intent = if (!userPreferences.getToken().isNullOrEmpty()) {
                val role = userPreferences.getUserRole() ?: "employee"
                if (role.equals("admin", ignoreCase = true)) {
                    Intent(this, AdminHomeActivity::class.java)
                } else {
                    Intent(this, EmployeeHomeActivity::class.java)
                }
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_DURATION)
    }
    
    private fun animateProgressBar(progressBar: ProgressBar) {
        val handler = Handler(Looper.getMainLooper())
        var progress = 0
        
        val runnable = object : Runnable {
            override fun run() {
                if (progress < 100) {
                    progress += 2
                    progressBar.progress = progress
                    handler.postDelayed(this, 30)
                }
            }
        }
        handler.post(runnable)
    }
}
