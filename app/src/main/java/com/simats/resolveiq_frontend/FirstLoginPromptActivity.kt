package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityFirstLoginPromptBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class FirstLoginPromptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstLoginPromptBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstLoginPromptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnContinueHome.setOnClickListener {
            navigateToDashboard()
        }

        binding.btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToDashboard() {
        val role = (userPreferences.getUserRole() ?: "EMPLOYEE").uppercase()
        val targetActivity = when (role) {
            "ADMIN" -> AdminHomeActivity::class.java
            "TEAM_LEAD" -> TeamLeadHomeActivity::class.java
            "AGENT" -> SupportAgentHomeActivity::class.java
            else -> EmployeeHomeActivity::class.java
        }
        val intent = Intent(this, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
