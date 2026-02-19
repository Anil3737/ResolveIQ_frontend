package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.simats.resolveiq_frontend.databinding.ActivityAdminHomeBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        setupUI()
        setupDrawer()
    }

    private fun setupUI() {
        // Open drawer on menu icon click
        binding.ivAdminMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Notification bell
        binding.ivNotification.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        // Profile icon
        binding.ivAdminProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // View All risky tickets
        binding.tvViewAllRisky.setOnClickListener {
            Toast.makeText(this, "View All Risky Tickets", Toast.LENGTH_SHORT).show()
        }

        // Bottom Navigation
        binding.adminBottomNavigation.selectedItemId = R.id.nav_admin_dashboard
        binding.adminBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> true
                R.id.nav_admin_tickets -> {
                    Toast.makeText(this, "Tickets", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_admin_users -> {
                    Toast.makeText(this, "Users", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_admin_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDrawer() {
        val navView = binding.navView

        // Dashboard (already on this page, just close drawer)
        navView.adminMenuDashboard.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Tickets
        navView.adminMenuTickets.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Tickets", Toast.LENGTH_SHORT).show()
        }

        // Teams
        navView.adminMenuTeams.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, TeamsActivity::class.java))
        }

        // Create Staff Dropdown Toggle
        navView.adminMenuCreateStaff.setOnClickListener {
            val isVisible = navView.llSubMenuStaff.visibility == android.view.View.VISIBLE
            if (isVisible) {
                navView.llSubMenuStaff.visibility = android.view.View.GONE
                navView.ivArrowStaff.setImageResource(R.drawable.ic_arrow_down)
            } else {
                navView.llSubMenuStaff.visibility = android.view.View.VISIBLE
                navView.ivArrowStaff.setImageResource(R.drawable.ic_arrow_up)
            }
        }

        // Sub-menu items
        navView.adminMenuCreateLead.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, CreateTeamLeadActivity::class.java))
        }

        navView.adminMenuCreateAgent.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Create Agent", Toast.LENGTH_SHORT).show()
        }

        // SLA Policies
        navView.adminMenuSlaPolicies.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "SLA Policies", Toast.LENGTH_SHORT).show()
        }

        // Escalations
        navView.adminMenuEscalations.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Escalations", Toast.LENGTH_SHORT).show()
        }

        // Reports
        navView.adminMenuReports.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Reports", Toast.LENGTH_SHORT).show()
        }

        // Settings
        navView.adminMenuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Logout
        navView.adminMenuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            userPreferences.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.adminBottomNavigation.selectedItemId = R.id.nav_admin_dashboard
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
