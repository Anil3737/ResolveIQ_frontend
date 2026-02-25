package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.simats.resolveiq_frontend.databinding.ActivitySettingsBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.switchDarkMode.isChecked = userPreferences.isDarkMode()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnProfileSettings.setOnClickListener {
            val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
            when (role) {
                "AGENT" -> startActivity(Intent(this, AgentProfileInfoActivity::class.java))
                "TEAM_LEAD" -> startActivity(Intent(this, TeamLeadProfileInfoActivity::class.java))
                else -> startActivity(Intent(this, ProfileInformationActivity::class.java))
            }
        }

        binding.btnSecuritySettings.setOnClickListener {
            Toast.makeText(this, "Security Settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelpCenter.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }



        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.saveDarkMode(isChecked)
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Bottom Navigation
        val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
        
        binding.bottomNavigation.menu.clear()
        when (role) {
            "ADMIN" -> binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_admin_menu)
            "TEAM_LEAD" -> binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_team_lead_menu)
            "AGENT" -> binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_agent_menu)
            else -> binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu)
        }

        binding.bottomNavigation.selectedItemId = when (role) {
            "ADMIN" -> R.id.nav_admin_settings
            "TEAM_LEAD" -> R.id.nav_tl_settings
            else -> R.id.nav_settings
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home, R.id.nav_admin_dashboard, R.id.nav_tl_dashboard -> {
                    val targetActivity = when (role) {
                        "ADMIN" -> AdminHomeActivity::class.java
                        "TEAM_LEAD" -> TeamLeadHomeActivity::class.java
                        "AGENT" -> SupportAgentHomeActivity::class.java
                        else -> EmployeeHomeActivity::class.java
                    }
                    val intent = Intent(this, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_tickets, R.id.nav_admin_tickets, R.id.nav_tl_tickets -> {
                    val intent = Intent(this, MyTicketsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_admin_users -> {
                    startActivity(Intent(this, UsersListActivity::class.java))
                    true
                }
                R.id.nav_activity, R.id.nav_admin_activity, R.id.nav_tl_activity -> {
                    if (role == "ADMIN") {
                        startActivity(Intent(this, AdminActivityLogActivity::class.java))
                    } else if (role == "TEAM_LEAD") {
                        Toast.makeText(this, "Team Activity coming soon", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_settings, R.id.nav_admin_settings, R.id.nav_tl_settings -> {
                    // Already on Settings
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
        binding.bottomNavigation.selectedItemId = when (role) {
            "ADMIN" -> R.id.nav_admin_settings
            "TEAM_LEAD" -> R.id.nav_tl_settings
            else -> R.id.nav_settings
        }
    }
}
