package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.MyTicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityAssignedTicketsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class AssignedTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignedTicketsBinding
    private lateinit var adapter: MyTicketAdapter
    private lateinit var repository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignedTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        repository = TicketRepository(RetrofitClient.getTicketApi(this))

        setupUI()
        setupBottomNavigation()
        fetchAssignedTickets()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = 0 // Not a primary tab
        fetchAssignedTickets()
    }

    private fun setupBottomNavigation() {
        // Assigned Queue is not a primary bottom navigation item, 
        // but we show it with 'Home' or 'Tickets' selected or none.
        // Let's keep it clear to avoid confusion.
        binding.bottomNavigation.selectedItemId = 0 // None selected
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, SupportAgentHomeActivity::class.java)
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
                    startActivity(Intent(this, AgentPerformanceActivity::class.java))
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

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.rvAssignedTickets.layoutManager = LinearLayoutManager(this)
        adapter = MyTicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, TicketDetailsActivity::class.java).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvAssignedTickets.adapter = adapter
    }

    private fun fetchAssignedTickets() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = repository.getTickets()
                if (result.isSuccess) {
                    val tickets = result.getOrDefault(emptyList())
                    val currentUserId = userPreferences.getUserId()
                    
                    // Filter for tickets specifically assigned to this agent
                    val assignedTickets = tickets.filter { it.assigned_to == currentUserId }
                    
                    adapter.updateTickets(assignedTickets)
                    
                    if (assignedTickets.isEmpty()) {
                        binding.tvNoTickets.visibility = View.VISIBLE
                        binding.rvAssignedTickets.visibility = View.GONE
                    } else {
                        binding.tvNoTickets.visibility = View.GONE
                        binding.rvAssignedTickets.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@AssignedTicketsActivity, "Failed to load tickets", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssignedTicketsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

}
