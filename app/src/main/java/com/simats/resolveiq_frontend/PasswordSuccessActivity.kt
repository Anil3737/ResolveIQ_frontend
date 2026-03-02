package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityPasswordSuccessBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class PasswordSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordSuccessBinding
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Clear user session as password has changed
        UserPreferences(this).clear()

        setupListeners()
        startRedirectTimer()
    }

    private fun setupListeners() {
        binding.btnContinueLogin.setOnClickListener {
            timer?.cancel()
            navigateToLogin()
        }
    }

    private fun startRedirectTimer() {
        timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvRedirectTimer.text = "Redirecting to login in $seconds seconds..."
            }

            override fun onFinish() {
                navigateToLogin()
            }
        }.start()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
