package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.AdminActivityAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.api.TeamLeadApiService
import com.simats.resolveiq_frontend.databinding.ActivityTeamLeadHomeBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class TeamLeadHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamLeadHomeBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var teamLeadApiService: TeamLeadApiService
    private lateinit var activityAdapter: AdminActivityAdapter
    private lateinit var authRepository: AuthRepository
    private lateinit var ticketRepository: TicketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamLeadHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        teamLeadApiService = RetrofitClient.getTeamLeadApi(this)
        authRepository = AuthRepository(RetrofitClient.getAuthApi(this))
        ticketRepository = TicketRepository(RetrofitClient.getTicketApi(this))
        setupUI()
        fetchTeamStats()
        fetchRecentActivity()
        fetchTeamPerformance()
    }

    private fun setupUI() {
        val storedName = userPreferences.getUserName() ?: "Team Lead"
        binding.tvWelcomeTL.text = "Welcome back, $storedName!"

        // Navigation Drawer
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.ivCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Profile Click
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, TeamLeadProfileInfoActivity::class.java))
        }

        // Quick Action Cards
        binding.btnTeamTickets.setOnClickListener {
            startActivity(Intent(this, TeamLeadTicketsActivity::class.java))
        }

        binding.btnHighRisk.setOnClickListener {
            val intent = Intent(this, MyTicketsActivity::class.java).apply {
                putExtra("filter_type", "HIGH_RISK")
                putExtra("department_name", binding.tvTeamName.text.toString())
            }
            startActivity(intent)
        }

        // ✅ Escalations now opens the new EscalationsActivity
        binding.btnEscalations.setOnClickListener {
            startActivity(Intent(this, TeamLeadEscalationsActivity::class.java))
        }

        binding.btnTeamMembers.setOnClickListener {
            startActivity(Intent(this, TeamMembersActivity::class.java))
        }

        // Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_tl_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tl_dashboard -> true
                R.id.nav_tl_tickets -> {
                    startActivity(Intent(this, TeamLeadTicketsActivity::class.java))
                    true
                }
                R.id.nav_tl_activity -> {
                    startActivity(Intent(this, TeamLeadActivityLogActivity::class.java))
                    true
                }
                R.id.nav_tl_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Dashboard View All Activity
        binding.tvViewAllActivity.setOnClickListener {
            startActivity(Intent(this, TeamLeadActivityLogActivity::class.java))
        }

        // Drawer Menu Click Handlers
        binding.navView.menuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navView.menuTeamTickets.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, TeamLeadTicketsActivity::class.java))
        }

        binding.navView.menuTeamMembers.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, TeamMembersActivity::class.java))
        }

        binding.navView.menuActivity.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, TeamLeadActivityLogActivity::class.java))
        }

        binding.navView.menuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.navView.menuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            performLogout()
        }

        // Initialize Activity Adapter
        activityAdapter = AdminActivityAdapter(emptyList()) { _ -> }
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(this@TeamLeadHomeActivity)
            adapter = activityAdapter
        }
    }

    private fun fetchTeamStats() {
        lifecycleScope.launch {
            // Fetch real department name from profile
            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    val deptName = user?.department_name ?: "Your Department"
                    binding.tvTeamName.text = deptName
                } else {
                    binding.tvTeamName.text = "Your Department"
                }
            } catch (e: Exception) {
                binding.tvTeamName.text = "Your Department"
            }

            // Fetch real ticket stats
            try {
                val ticketResult = ticketRepository.getTickets()
                if (ticketResult.isSuccess) {
                    val tickets = ticketResult.getOrNull() ?: emptyList()
                    val activeCount = tickets.count { it.status in listOf("OPEN", "IN_PROGRESS", "APPROVED", "ESCALATED") }
                    val highRiskCount = tickets.count { (it.ai_score ?: 0) >= 70 || (it.breach_risk ?: 0) >= 70 }
                    val resolvedCount = tickets.count { it.status in listOf("RESOLVED", "CLOSED") }

                    binding.tvActiveCount.text = activeCount.toString()
                    binding.tvSlaRiskCount.text = highRiskCount.toString()
                    binding.tvResolvedCount.text = resolvedCount.toString()

                    binding.tvWelcomeStatus.text = "You have $activeCount active tickets and $highRiskCount high-risk cases."
                } else {
                    binding.tvActiveCount.text = "0"
                    binding.tvSlaRiskCount.text = "0"
                    binding.tvResolvedCount.text = "0"
                    binding.tvWelcomeStatus.text = "Failed to load stats."
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Fetch team members and compute real Team Performance stats:
     * - Blue card: Total resolved today across all members
     * - Green card: Team workload % (active / capacity × 100)
     */
    private fun fetchTeamPerformance() {
        lifecycleScope.launch {
            try {
                val response = teamLeadApiService.getTeamMembers()
                if (response.success) {
                    val members = response.data ?: emptyList()

                    if (members.isNotEmpty()) {
                        val totalResolved = members.sumOf { it.resolved_today }
                        val totalActive = members.sumOf { it.active_tickets }
                        val totalCapacity = members.sumOf { it.daily_capacity }
                        val workloadPct = if (totalCapacity > 0) {
                            ((totalActive.toFloat() / totalCapacity.toFloat()) * 100).toInt()
                        } else 0

                        // Blue card — total resolved today + member count
                        binding.tvAvgResolutionValue.text = totalResolved.toString()
                        binding.tvAvgResolutionTrend.text = "${members.size} agents"

                        // Green card — workload %
                        binding.tvTeamLoadValue.text = "$workloadPct%"
                        val loadLabel = when {
                            workloadPct < 40 -> "Low Load"
                            workloadPct < 70 -> "Medium Load"
                            else -> "High Load"
                        }
                        binding.tvTeamLoadTrend.text = loadLabel
                    } else {
                        binding.tvAvgResolutionValue.text = "0"
                        binding.tvAvgResolutionTrend.text = "0 agents"
                        binding.tvTeamLoadValue.text = "0%"
                        binding.tvTeamLoadTrend.text = "No data"
                    }
                }
            } catch (e: Exception) {
                // Silent fail — keep showing dashes
            }
        }
    }

    private fun fetchRecentActivity() {
        lifecycleScope.launch {
            try {
                // Fetch top 5 recent activities for the dashboard
                val response = teamLeadApiService.getTeamActivityLog()
                if (response.success) {
                    val recentLogs = response.logs.take(5)
                    activityAdapter.updateData(recentLogs)
                }
            } catch (e: Exception) {
                // Silent fail for dashboard
            }
        }
    }

    private fun performLogout() {
        userPreferences.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_tl_dashboard
        fetchTeamStats()
        fetchRecentActivity()
        fetchTeamPerformance()
    }
}
