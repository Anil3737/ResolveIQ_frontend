package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.simats.resolveiq_frontend.databinding.ActivityTeamLeadHomeBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class TeamLeadHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamLeadHomeBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamLeadHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        setupUI()
        loadTeamStats()
    }

    private fun setupUI() {
        val storedName = userPreferences.getUserName() ?: "Team Lead"
        binding.tvWelcomeTL.text = "Welcome back, $storedName!"

        // Navigation Drawer
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.ivCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Profile Click
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, TeamLeadProfileInfoActivity::class.java))
        }

        // Quick Action Cards
        binding.btnTeamTickets.setOnClickListener {
            startActivity(Intent(this, TeamLeadTicketsActivity::class.java))
        }

        binding.btnHighRisk.setOnClickListener {
            Toast.makeText(this, "Focusing on High Risk Tickets", Toast.LENGTH_SHORT).show()
        }

        binding.btnEscalations.setOnClickListener {
            Toast.makeText(this, "Viewing Pending Escalations", Toast.LENGTH_SHORT).show()
        }

        binding.btnTeamMembers.setOnClickListener {
            startActivity(Intent(this, TeamMembersActivity::class.java))
        }

        // Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_tl_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tl_dashboard -> true
                R.id.nav_tl_tickets -> {
                    startActivity(Intent(this, TeamLeadTicketsActivity::class.java))
                    true
                }
                R.id.nav_tl_activity -> {
                    Toast.makeText(this, "Team Activity Log", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_tl_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Drawer Menu Click Handlers
        binding.navView.menuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navView.menuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            performLogout()
        }
    }

    private fun loadTeamStats() {
        // Mock data loading - in real app, these would come from TicketRepository
        // filtered by the team lead's team_id
        binding.tvTeamName.text = "Network & VPN Team"
        binding.tvWelcomeStatus.text = "You have 12 active tickets and 3 high-risk cases."
        
        binding.tvActiveCount.text = "12"
        binding.tvSlaRiskCount.text = "3"
        binding.tvResolvedCount.text = "8"
    }

    private fun performLogout() {
        userPreferences.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
