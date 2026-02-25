package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.simats.resolveiq_frontend.databinding.ActivityAdminHomeBinding
import com.simats.resolveiq_frontend.utils.UserPreferences
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simats.resolveiq_frontend.data.model.AdminDashboardResponse
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.api.AdminApiService
import com.simats.resolveiq_frontend.adapter.MyTicketAdapter

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var adminApiService: AdminApiService
    private lateinit var ticketAdapter: MyTicketAdapter
    private var allTickets: List<Ticket> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        adminApiService = com.simats.resolveiq_frontend.api.RetrofitClient.getAdminApi(this)
        
        setupUI()
        setupRecyclerView()
        setupDrawer()
        fetchDashboardData()
    }

    private fun setupRecyclerView() {
        ticketAdapter = MyTicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, AdminTicketDetailActivity::class.java).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvRiskyTickets.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@AdminHomeActivity)
            adapter = ticketAdapter
        }
    }

    private fun fetchDashboardData() {
        binding.swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            try {
                val response = adminApiService.getDashboardData()
                if (response.success) {
                    updateDashboardUI(response)
                } else {
                    Toast.makeText(this@AdminHomeActivity, response.message ?: "Failed to fetch dashboard data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminHomeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateDashboardUI(response: AdminDashboardResponse) {
        val metrics = response.metrics
        val dist = response.riskDistribution
        
        // 1. Update Metric Cards
        binding.tvTotalTickets.text = java.text.NumberFormat.getInstance().format(metrics.totalTickets)
        binding.tvHighRisk.text = metrics.highRisk.toString()
        binding.tvSlaBreached.text = metrics.slaBreached.toString()
        binding.tvEscalated.text = String.format("%02d", metrics.escalated)
        
        // 2. Update Risk Distribution Text
        val totalTickets = metrics.totalTickets
        val formattedTotal = if (totalTickets >= 1000) String.format("%.1fk", totalTickets / 1000.0) else totalTickets.toString()
        binding.tvDonutTotal.text = formattedTotal

        // Helper to calculate percentage and format string
        fun getLegendText(label: String, value: Int): String {
            val percent = if (totalTickets > 0) (value * 100 / totalTickets) else 0
            return "$label ($percent%)"
        }

        binding.tvCriticalLegend.text = getLegendText("Critical", dist.critical)
        binding.tvHighLegend.text = getLegendText("High", dist.high)
        binding.tvMediumLegend.text = getLegendText("Medium", dist.medium)
        binding.tvLowLegend.text = getLegendText("Low", dist.low)

        // 3. Update Top Risky Tickets List
        val riskyTickets = response.topRiskyTickets.map { t ->
            Ticket(
                id = t.id,
                ticket_number = t.ticketNumber,
                title = t.title,
                status = t.status,
                ai_score = t.aiScore,
                description = "",       // Not needed for home preview
                priority = "",
                department_id = 0,
                department_name = null,
                created_at = "",
                breach_risk = t.aiScore,
                sla_breached = false,
                created_by_name = null,
                created_by_emp_id = null,
                sla_deadline = null,
                sla_remaining_seconds = null
            )
        }
        ticketAdapter.updateTickets(riskyTickets)
    }

    private fun setupUI() {
        // Pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchDashboardData()
        }

        // Open drawer on menu icon click
        binding.ivAdminMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        
        val storedName = userPreferences.getUserName() ?: "Admin"
        binding.tvWelcomeAdmin.text = "Welcome back, $storedName!"

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
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "HIGH_RISK")
            }
            startActivity(intent)
        }

        // Metric Card Clicks
        binding.llTotalTicketsCard.setOnClickListener {
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "ALL")
            }
            startActivity(intent)
        }

        binding.llHighRiskCard.setOnClickListener {
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "HIGH_RISK")
            }
            startActivity(intent)
        }

        binding.llSlaBreachedCard.setOnClickListener {
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "SLA_BREACHED")
            }
            startActivity(intent)
        }

        binding.llEscalatedCard.setOnClickListener {
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "ESCALATED")
            }
            startActivity(intent)
        }

        // Bottom Navigation
        binding.adminBottomNavigation.selectedItemId = R.id.nav_admin_dashboard
        binding.adminBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> true
                R.id.nav_admin_tickets -> {
                    startActivity(Intent(this, MyTicketsActivity::class.java))
                    true
                }
                R.id.nav_admin_users -> {
                    startActivity(Intent(this, UsersListActivity::class.java))
                    true
                }
                R.id.nav_admin_activity -> {
                    startActivity(Intent(this, AdminActivityLogActivity::class.java))
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
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "ALL")
            }
            startActivity(intent)
        }

        // Teams
        navView.adminMenuTeams.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, TeamsActivity::class.java))
        }

        // Employees
        navView.adminMenuEmployees.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, UsersListActivity::class.java))
        }

        // System Activity
        navView.adminMenuActivity.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, AdminActivityLogActivity::class.java))
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
            startActivity(Intent(this, CreateAgentActivity::class.java))
        }

        // SLA Policies
        navView.adminMenuSlaPolicies.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "SLA_BREACHED")
            }
            startActivity(intent)
        }

        // Escalations
        navView.adminMenuEscalations.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "ESCALATED")
            }
            startActivity(intent)
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
