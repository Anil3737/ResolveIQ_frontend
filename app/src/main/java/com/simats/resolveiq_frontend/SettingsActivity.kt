package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnProfileSettings.setOnClickListener {
            Toast.makeText(this, "Profile Settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSecuritySettings.setOnClickListener {
            Toast.makeText(this, "Security Settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelpCenter.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, WorkflowActivity::class.java))
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Dark Mode $status", Toast.LENGTH_SHORT).show()
        }
    }
}
