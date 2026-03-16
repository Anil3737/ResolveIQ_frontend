package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivitySettingsBinding
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

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
        
        val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
        if (role == "ADMIN") {
            binding.vSeparatorResetRequests.visibility = View.VISIBLE
            binding.btnPasswordResetRequests.visibility = View.VISIBLE
            fetchPendingResetCount()
        }
    }

    private fun fetchPendingResetCount() {
        lifecycleScope.launch {
            try {
                val adminApi = RetrofitClient.getAdminApi(this@SettingsActivity)
                val response = adminApi.getDashboardData()
                val count = response.metrics?.pendingResetCount ?: 0
                if (count > 0) {
                    binding.tvResetRequestsBadge.text = count.toString()
                    binding.tvResetRequestsBadge.visibility = View.VISIBLE
                } else {
                    binding.tvResetRequestsBadge.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnPasswordResetRequests.setOnClickListener {
            startActivity(Intent(this, PasswordResetRequestsActivity::class.java))
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
            startActivity(Intent(this, ChangePasswordActivity::class.java))
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
                    val targetActivity = when (role) {
                        "ADMIN" -> AdminGroupedTicketsActivity::class.java
                        "TEAM_LEAD" -> TeamLeadTicketsActivity::class.java
                        "AGENT" -> MyTicketsActivity::class.java
                        else -> MyTicketsActivity::class.java
                    }
                    val intent = Intent(this, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_admin_users -> {
                    startActivity(Intent(this, UsersListActivity::class.java))
                    true
                }
                R.id.nav_activity, R.id.nav_admin_activity, R.id.nav_tl_activity -> {
                    val targetActivity = when (role) {
                        "ADMIN" -> AdminActivityLogActivity::class.java
                        "AGENT" -> AgentPerformanceActivity::class.java
                        "TEAM_LEAD" -> TeamLeadActivityLogActivity::class.java
                        else -> null
                    }
                    if (targetActivity != null) {
                        val intent = Intent(this, targetActivity)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
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
