package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.MyTicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityMyTicketsBinding
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MyTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyTicketsBinding
    private lateinit var adapter: MyTicketAdapter
    private lateinit var ticketRepository: TicketRepository
    private lateinit var userPreferences: UserPreferences
    private var allTickets: List<Ticket> = emptyList()
    private var isShowingActive = true
    private var filterType: String? = null
    private var fetchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupUI()
        fetchTickets()
    }

    private fun setupDependencies() {
        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
        userPreferences = UserPreferences(this)
    }

    private fun setupUI() {
        filterType = intent.getStringExtra("filter_type")
        
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Adjust UI if a specific metric filter is applied
        if (filterType != null && filterType != "ALL") {
            binding.tabLayout.visibility = View.GONE
            binding.tvHeaderTitle.text = when (filterType) {
                "HIGH_RISK" -> "High Risk Tickets"
                "SLA_BREACHED" -> "SLA Breached Tickets"
                "ESCALATED" -> "Escalated Tickets"
                else -> "Tickets"
            }
        }

        binding.rvMyTickets.layoutManager = LinearLayoutManager(this)
        adapter = MyTicketAdapter(emptyList()) { ticket ->
            val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
            val targetActivity = if (role == "ADMIN") AdminTicketDetailActivity::class.java else TicketDetailsActivity::class.java
            val intent = android.content.Intent(this, targetActivity).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvMyTickets.adapter = adapter

        binding.btnActiveTab.setOnClickListener {
            updateTabs(isActive = true)
            filterTickets(isActive = true)
        }

        binding.btnResolvedTab.setOnClickListener {
            updateTabs(isActive = false)
            filterTickets(isActive = false)
        }

        // Bottom Navigation
        val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
        if (role == "ADMIN") {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_admin_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_admin_tickets
        }

        binding.bottomNavigation.selectedItemId = if (role == "ADMIN") R.id.nav_admin_tickets else R.id.nav_tickets
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home, R.id.nav_admin_dashboard -> {
                    val targetActivity = if (role == "ADMIN") AdminHomeActivity::class.java else EmployeeHomeActivity::class.java
                    val intent = android.content.Intent(this, targetActivity)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_tickets, R.id.nav_admin_tickets -> {
                    true
                }
                R.id.nav_admin_users -> {
                    startActivity(android.content.Intent(this, UsersListActivity::class.java))
                    true
                }
                R.id.nav_activity, R.id.nav_admin_activity -> {
                    if (role == "ADMIN") {
                        startActivity(android.content.Intent(this, AdminActivityLogActivity::class.java))
                    } else {
                        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_settings, R.id.nav_admin_settings -> {
                    val intent = android.content.Intent(this, SettingsActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val role = userPreferences.getUserRole()?.uppercase() ?: "EMPLOYEE"
        binding.bottomNavigation.selectedItemId =
            if (role == "ADMIN") R.id.nav_admin_tickets else R.id.nav_tickets
        fetchTickets()
    }

    private fun updateTabs(isActive: Boolean) {
        isShowingActive = isActive
        if (isActive) {
            binding.btnActiveTab.setBackgroundResource(R.drawable.button_primary)
            binding.btnActiveTab.backgroundTintList = getColorStateList(android.R.color.white)
            binding.btnActiveTab.setTextColor(getColor(R.color.blue_600))
            binding.btnActiveTab.setTypeface(null, android.graphics.Typeface.BOLD)

            binding.btnResolvedTab.background = null
            binding.btnResolvedTab.setTextColor(getColor(android.R.color.white))
            binding.btnResolvedTab.alpha = 0.7f
            binding.btnResolvedTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            binding.btnResolvedTab.setBackgroundResource(R.drawable.button_primary)
            binding.btnResolvedTab.backgroundTintList = getColorStateList(android.R.color.white)
            binding.btnResolvedTab.setTextColor(getColor(R.color.blue_600))
            binding.btnResolvedTab.setTypeface(null, android.graphics.Typeface.BOLD)

            binding.btnActiveTab.background = null
            binding.btnActiveTab.setTextColor(getColor(android.R.color.white))
            binding.btnActiveTab.alpha = 0.7f
            binding.btnActiveTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun fetchTickets() {
        fetchJob?.cancel()
        fetchJob = lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = ticketRepository.getTickets()
                if (result.isSuccess) {
                    allTickets = result.getOrDefault(emptyList())
                    filterTickets(isShowingActive)
                } else {
                    val error = result.exceptionOrNull()
                    if (error !is kotlinx.coroutines.CancellationException) {
                        Toast.makeText(this@MyTicketsActivity, "Failed to load tickets: ${error?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterTickets(isActive: Boolean) {
        var filtered = if (isActive) {
            allTickets.filter { it.status.lowercase() !in listOf("resolved", "closed") }
        } else {
            allTickets.filter { it.status.lowercase() in listOf("resolved", "closed") }
        }

        // Apply metric filters if any
        filterType?.let { type ->
            filtered = when (type) {
                "HIGH_RISK"    -> allTickets.filter { (it.ai_score ?: 0) >= 70 || (it.breach_risk ?: 0) >= 70 }
                "SLA_BREACHED" -> allTickets.filter { it.sla_breached == true }
                "ESCALATED"    -> allTickets.filter { it.status.uppercase() == "ESCALATED" }
                "ALL" -> {
                    // Reset tab layout visibility just in case
                    binding.tabLayout.visibility = View.VISIBLE
                    if (isActive) {
                        allTickets.filter { it.status.lowercase() !in listOf("resolved", "closed") }
                    } else {
                        allTickets.filter { it.status.lowercase() in listOf("resolved", "closed") }
                    }
                }
                else -> filtered
            }
        }

        adapter.updateTickets(filtered)
        
        if (filtered.isEmpty()) {
            binding.tvNoTickets.visibility = View.VISIBLE
            binding.rvMyTickets.visibility = View.GONE
        } else {
            binding.tvNoTickets.visibility = View.GONE
            binding.rvMyTickets.visibility = View.VISIBLE
        }
    }
}

