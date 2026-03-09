package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.databinding.ActivityAgentPerformanceBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgentPerformanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgentPerformanceBinding
    private lateinit var repository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgentPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        repository = TicketRepository(RetrofitClient.getTicketApi(this))

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupBottomNavigation()
        fetchPerformanceData()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_activity
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_activity
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = android.content.Intent(this, SupportAgentHomeActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_tickets -> {
                    val intent = android.content.Intent(this, MyTicketsActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_activity -> true
                R.id.nav_settings -> {
                    val intent = android.content.Intent(this, SettingsActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchPerformanceData() {
        lifecycleScope.launch {
            try {
                val result = repository.getTickets()
                if (result.isSuccess) {
                    val allTickets = result.getOrDefault(emptyList())
                    val agentName = userPreferences.getUserName()
                    
                    // Filter tickets assigned to this agent
                    val myTickets = allTickets.filter { it.assigned_to_name == agentName }
                    
                    calculateAndDisplayStats(myTickets)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateAndDisplayStats(tickets: List<com.simats.resolveiq_frontend.data.model.Ticket>) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // 1. Solved Today
        val solvedToday = tickets.count { 
            it.resolved_at?.startsWith(today) == true 
        }
        
        // 2. SLA Breached
        val slaBreached = tickets.count { it.sla_breached == true }
        
        // 3. Escalated
        val escalated = tickets.count { it.status.uppercase() == "ESCALATED" }
        
        // 4. Average Resolved per Day
        val resolvedTickets = tickets.filter { it.resolved_at != null }
        val resolvedDates = resolvedTickets.mapNotNull { it.resolved_at?.substring(0, 10) }.distinct()
        val avgResolved = if (resolvedDates.isNotEmpty()) {
            resolvedTickets.size.toDouble() / resolvedDates.size
        } else {
            0.0
        }

        // 5. Efficiency (example: resolved vs total assigned)
        val efficiency = if (tickets.isNotEmpty()) {
            (resolvedTickets.size.toDouble() / tickets.size * 100).toInt()
        } else {
            0
        }

        // Update UI
        binding.tvSolvedToday.text = solvedToday.toString()
        binding.tvSlaBreached.text = slaBreached.toString()
        binding.tvEscalated.text = escalated.toString()
        binding.tvAvgResolved.text = String.format("%.1f", avgResolved)
        binding.tvEfficiency.text = "$efficiency%"
        
        // Performance Status
        binding.tvPerformanceStatus.text = when {
            efficiency >= 90 -> "EXCELLENT"
            efficiency >= 75 -> "GOOD"
            efficiency >= 50 -> "AVERAGE"
            else -> "NEEDS IMPROVEMENT"
        }
    }
}
