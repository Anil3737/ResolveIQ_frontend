package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityTicketSuccessBinding

class TicketSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnCheckProgress.setOnClickListener {
            // Navigate back to Home (User can see tickets there)
            // Or explicitly to My Tickets logic if separate
            val intent = Intent(this, EmployeeHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // Bottom Navigation
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
