package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.simats.resolveiq_frontend.databinding.ActivitySupportAgentHomeBinding

class SupportAgentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportAgentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportAgentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupBottomNavigation()
    }

    private fun setupListeners() {
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.ivAgentProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
                R.id.nav_create -> {
                    startActivity(Intent(this, CreateTicketActivity::class.java))
                    true
                }
                R.id.nav_alerts -> {
                    Toast.makeText(this, "Alerts coming soon...", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
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
