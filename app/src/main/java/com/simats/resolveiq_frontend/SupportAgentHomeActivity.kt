package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.databinding.ActivitySupportAgentHomeBinding
import kotlinx.coroutines.launch

class SupportAgentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportAgentHomeBinding
    private lateinit var userPreferences: com.simats.resolveiq_frontend.utils.UserPreferences
    private lateinit var repository: com.simats.resolveiq_frontend.repository.TicketRepository

    private lateinit var adapter: com.simats.resolveiq_frontend.adapter.HighPriorityTicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportAgentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = com.simats.resolveiq_frontend.utils.UserPreferences(this)
        repository = com.simats.resolveiq_frontend.repository.TicketRepository(
            com.simats.resolveiq_frontend.api.RetrofitClient.getTicketApi(this)
        )
        
        adapter = com.simats.resolveiq_frontend.adapter.HighPriorityTicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, TicketDetailsActivity::class.java).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvHighPriorityQueue.adapter = adapter
        
        val storedName = userPreferences.getUserName() ?: "Agent"
        binding.tvGreetingAgent.text = "Good morning, $storedName"

        setupListeners()
        setupBottomNavigation()
        setupDrawer()
        setupBackPressHandler()
        fetchDashboardStats()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        fetchDashboardStats()
    }

    private fun fetchDashboardStats() {
        lifecycleScope.launch {
            try {
                val result = repository.getTickets()
                if (result.isSuccess) {
                    val tickets = result.getOrDefault(emptyList())
                    
                    val activeCount = tickets.count { 
                        it.status.uppercase() in listOf("OPEN", "APPROVED", "IN_PROGRESS") 
                    }
                    val resolvedCount = tickets.count { 
                        it.status.uppercase() in listOf("RESOLVED", "CLOSED") 
                    }
                    val breachCount = tickets.count { it.sla_breached == true }
                    
                    binding.tvActiveTicketsCount.text = activeCount.toString()
                    binding.tvResolvedTicketsCount.text = resolvedCount.toString()
                    binding.tvBreachTicketsCount.text = breachCount.toString()

                    // Filter for High Priority Queue: P1, P2, or High Risk (>= 70)
                    val highPriorityTickets = tickets.filter {
                        val status = it.status.uppercase()
                        val risk = it.ai_score ?: it.breach_risk ?: 0
                        (status in listOf("OPEN", "APPROVED", "IN_PROGRESS", "PENDING")) &&
                        (it.priority.uppercase() in listOf("P1", "P2") || risk >= 70)
                    }.sortedByDescending { it.ai_score ?: it.breach_risk ?: 0 }
                    
                    adapter.updateTickets(highPriorityTickets)
                }
            } catch (e: Exception) {
                // Silently fail for stats
            }
        }
    }

    private fun setupListeners() {
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        binding.ivAgentProfile.setOnClickListener {
            startActivity(Intent(this, AgentProfileInfoActivity::class.java))
        }


        binding.cardAssignedQueue.setOnClickListener {
            startActivity(Intent(this, AssignedTicketsActivity::class.java))
        }

        binding.cardKnowledgeBase.setOnClickListener {
            startActivity(Intent(this, KnowledgeBaseActivity::class.java))
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

    private fun setupDrawer() {
        val navBinding = binding.navView

        navBinding.agentMenuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        navBinding.agentMenuAssignedQueue.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, AssignedTicketsActivity::class.java))
        }

        navBinding.agentMenuKnowledge.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, KnowledgeBaseActivity::class.java))
        }

        navBinding.agentMenuTickets.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }

        navBinding.agentMenuPerformance.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, AgentPerformanceActivity::class.java))
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

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
