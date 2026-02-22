package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityTicketSuccessBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class TicketSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketSuccessBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userPreferences = UserPreferences(this)
        
        binding.btnCheckProgress.setOnClickListener {
            // Navigate back to Home based on role
            val role = userPreferences.getUserRole() ?: "employee"
            val targetActivity = if (role.equals("admin", ignoreCase = true)) {
                AdminHomeActivity::class.java
            } else {
                EmployeeHomeActivity::class.java
            }
            val intent = Intent(this, targetActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val role = userPreferences.getUserRole() ?: "employee"
                    val targetActivity = if (role.equals("admin", ignoreCase = true)) {
                        AdminHomeActivity::class.java
                    } else {
                        EmployeeHomeActivity::class.java
                    }
                    val intent = Intent(this, targetActivity)
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
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = 0 // Unselect
    }
}
