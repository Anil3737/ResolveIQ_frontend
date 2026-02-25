package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.simats.resolveiq_frontend.databinding.ActivitySupportAgentHomeBinding

class SupportAgentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportAgentHomeBinding
    private lateinit var userPreferences: com.simats.resolveiq_frontend.utils.UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportAgentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = com.simats.resolveiq_frontend.utils.UserPreferences(this)
        val storedName = userPreferences.getUserName() ?: "Agent"
        binding.tvGreetingAgent.text = "Good morning, $storedName"

        setupListeners()
        setupBottomNavigation()
        setupDrawer()
    }

    private fun setupListeners() {
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.ivAgentProfile.setOnClickListener {
            startActivity(Intent(this, AgentProfileInfoActivity::class.java))
        }

        binding.ivSearch.setOnClickListener {
            Toast.makeText(this, "Search coming soon...", Toast.LENGTH_SHORT).show()
        }

        binding.fabCreate.setOnClickListener {
            startActivity(Intent(this, CreateTicketActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_tickets -> {
                    startActivity(Intent(this, MyTicketsActivity::class.java))
                    true
                }
                R.id.nav_activity -> {
                    Toast.makeText(this, "Activity log coming soon...", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDrawer() {
        val navBinding = binding.navView

        navBinding.agentMenuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        navBinding.agentMenuTickets.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }

        navBinding.agentMenuKnowledge.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Knowledge Base coming soon...", Toast.LENGTH_SHORT).show()
        }

        navBinding.agentMenuNotifications.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Notifications coming soon...", Toast.LENGTH_SHORT).show()
        }

        navBinding.agentMenuPerformance.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Performance coming soon...", Toast.LENGTH_SHORT).show()
        }

        navBinding.agentMenuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        navBinding.agentMenuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            userPreferences.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
