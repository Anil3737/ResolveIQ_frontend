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

        // Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, EmployeeHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_tickets -> {
                    val intent = Intent(this, MyTicketsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_activity -> {
                    Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_settings -> {
                    // Already on Settings
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
    }
}
