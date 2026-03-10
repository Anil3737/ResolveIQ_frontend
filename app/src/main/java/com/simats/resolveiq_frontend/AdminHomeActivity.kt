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
import android.graphics.Color
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var adminApiService: AdminApiService
    private lateinit var ticketApiService: com.simats.resolveiq_frontend.api.TicketApiService
    private lateinit var ticketAdapter: MyTicketAdapter
    private var allTickets: List<Ticket> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        adminApiService = com.simats.resolveiq_frontend.api.RetrofitClient.getAdminApi(this)
        ticketApiService = com.simats.resolveiq_frontend.api.RetrofitClient.getTicketApi(this)
        
        setupUI()
        setupRiskChart()
        setupRecyclerView()
        setupDrawer()
        setupBackPressHandler()
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
                // Fetch dashboard summary
                val response = adminApiService.getDashboardData()
                
                // Also fetch all tickets to ensure metrics and lists are accurate
                val ticketsResult = ticketApiService.getTickets()
                
                if (response.success && ticketsResult.success) {
                    allTickets = ticketsResult.data ?: emptyList()
                    updateDashboardWithAccuracy(response, allTickets)
                } else if (response.success) {
                    // Fallback to limited dashboard data if full list fails
                    updateDashboardUI(response)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminHomeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateDashboardWithAccuracy(response: AdminDashboardResponse, tickets: List<Ticket>) {
        // 1. Recalculate metrics from ALL tickets for maximum accuracy
        val totalTickets = tickets.size
        val highRiskCount = tickets.count { (it.ai_score ?: it.breach_risk ?: 0) >= 70 }
        val slaBreachedCount = tickets.count { it.sla_breached == true }
        // Look for any indicator of escalation, but exclude RESOLVED/CLOSED tickets
        val escalatedCount = tickets.count { 
            (it.status.equals("ESCALATED", true) || 
             it.status.equals("HIGH_RISK", true) ||
             (it.ai_score ?: 0) >= 80) && 
            !it.status.equals("RESOLVED", true) && 
            !it.status.equals("CLOSED", true)
        }

        binding.tvTotalTickets.text = java.text.NumberFormat.getInstance().format(totalTickets)
        binding.tvHighRisk.text = highRiskCount.toString()
        binding.tvSlaBreached.text = slaBreachedCount.toString()
        binding.tvEscalated.text = String.format("%02d", escalatedCount)

        // 2. Risk Distribution (using the provided distribution or recalculating)
        // Recalculating distribution for consistency
        val critical = tickets.count { (it.ai_score ?: it.breach_risk ?: 0) >= 90 }
        val high = tickets.count { (it.ai_score ?: it.breach_risk ?: 0) in 70..89 }
        val medium = tickets.count { (it.ai_score ?: it.breach_risk ?: 0) in 40..69 }
        val low = tickets.count { (it.ai_score ?: it.breach_risk ?: 0) < 40 }

        val formattedTotal = if (totalTickets >= 1000) String.format("%.1fk", totalTickets / 1000.0) else totalTickets.toString()
        binding.tvDonutTotal.text = formattedTotal

        fun getLegendText(label: String, value: Int): String {
            val percent = if (totalTickets > 0) (value * 100 / totalTickets) else 0
            return "$label ($percent%)"
        }

        binding.tvCriticalLegend.text = getLegendText("Critical", critical)
        binding.tvHighLegend.text = getLegendText("High", high)
        binding.tvMediumLegend.text = getLegendText("Medium", medium)
        binding.tvLowLegend.text = getLegendText("Low", low)

        // 3. Top Risky Tickets - use the full list to ensure the 91% ticket is included
        val topRisky = tickets
            .filter { (it.ai_score ?: it.breach_risk ?: 0) >= 70 }
            .sortedByDescending { it.ai_score ?: it.breach_risk ?: 0 }
            .take(5) // Show top 5 on home page

        ticketAdapter.updateTickets(topRisky)
        updateRiskChart(critical, high, medium, low)
    }

    private fun updateRiskChart(critical: Int, high: Int, medium: Int, low: Int) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        if (critical > 0) {
            entries.add(PieEntry(critical.toFloat(), ""))
            colors.add(Color.parseColor("#EF4444")) // Critical - Red
        }
        if (high > 0) {
            entries.add(PieEntry(high.toFloat(), ""))
            colors.add(Color.parseColor("#F97316")) // High - Orange
        }
        if (medium > 0) {
            entries.add(PieEntry(medium.toFloat(), ""))
            colors.add(Color.parseColor("#FBBF24")) // Medium - Amber
        }
        if (low > 0) {
            entries.add(PieEntry(low.toFloat(), ""))
            colors.add(Color.parseColor("#3B82F6")) // Low - Blue
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 4f
            setDrawValues(false)
        }

        binding.riskPieChart.apply {
            data = PieData(dataSet)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupRiskChart() {
        binding.riskPieChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 75f // Large hole for modern donut look
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false)
            setTouchEnabled(false) // Purely visual or handle clicks if needed
        }
    }

    private fun updateDashboardUI(response: AdminDashboardResponse) {
        val metrics = response.metrics
        val dist = response.riskDistribution
        
        // 1. Update Metric Cards
        binding.tvTotalTickets.text = java.text.NumberFormat.getInstance().format(metrics.totalTickets)
        binding.tvHighRisk.text = metrics.highRisk.toString()
        binding.tvSlaBreached.text = metrics.slaBreached.toString()
        val escalated = if (metrics.escalated == 0) {
            allTickets.count { 
                it.status.equals("ESCALATED", true) && 
                !it.status.equals("RESOLVED", true) && 
                !it.status.equals("CLOSED", true) 
            }
        } else metrics.escalated
        binding.tvEscalated.text = String.format("%02d", escalated)
        
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

        // 3. Update Top Risky Tickets List — sorted highest risk score first
        val riskyTickets = response.topRiskyTickets
            .sortedByDescending { it.aiScore }
            .map { t ->
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
        updateRiskChart(dist.critical, dist.high, dist.medium, dist.low)
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
            startActivity(Intent(this, ProfileInfoActivity::class.java))
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
                    startActivity(Intent(this, AdminGroupedTicketsActivity::class.java))
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
            startActivity(Intent(this, AdminGroupedTicketsActivity::class.java))
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
            startActivity(Intent(this, SlaPoliciesActivity::class.java))
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
            startActivity(Intent(this, AdminReportsActivity::class.java))
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
