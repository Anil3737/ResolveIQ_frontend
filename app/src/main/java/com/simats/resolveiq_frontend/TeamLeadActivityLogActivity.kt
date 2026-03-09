package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.simats.resolveiq_frontend.adapter.AdminActivityAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.api.TeamLeadApiService
import com.simats.resolveiq_frontend.data.model.AdminActivityLog
import com.simats.resolveiq_frontend.databinding.ActivityTeamLeadActivityLogBinding
import kotlinx.coroutines.launch

class TeamLeadActivityLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamLeadActivityLogBinding
    private lateinit var teamLeadApiService: TeamLeadApiService
    private lateinit var adapter: AdminActivityAdapter

    // Full list of all loaded logs
    private var allLogs: List<AdminActivityLog> = emptyList()

    // Tab categories
    private val ticketActions = setOf(
        "TICKET_CREATED", "TICKET_ASSIGNED", "STATUS_UPDATED",
        "AUTO_ESCALATED", "SLA_BREACHED", "AUTO_CLOSED", "MANUAL_CLOSED"
    )
    private val systemActions = setOf("USER_CREATED", "USER_LOGIN")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamLeadActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teamLeadApiService = RetrofitClient.getTeamLeadApi(this)
        setupUI()
        fetchActivityLogs()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivRefresh.setOnClickListener { fetchActivityLogs() }

        adapter = AdminActivityAdapter(emptyList()) { /* detail click if needed */ }

        binding.rvActivityLog.apply {
            layoutManager = LinearLayoutManager(this@TeamLeadActivityLogActivity)
            adapter = this@TeamLeadActivityLogActivity.adapter
        }

        // Tab filtering
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                applyTabFilter(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun applyTabFilter(tabIndex: Int) {
        val filtered = when (tabIndex) {
            1 -> allLogs.filter { it.action_type in ticketActions }  // Tickets
            2 -> allLogs.filter { it.action_type in systemActions }  // Login / System
            else -> allLogs                                           // All
        }

        if (filtered.isEmpty()) {
            binding.rvActivityLog.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvActivitySubtitle.text = "No entries in this category"
        } else {
            binding.tvEmpty.visibility = View.GONE
            adapter.updateData(filtered)
            binding.rvActivityLog.visibility = View.VISIBLE
            binding.tvActivitySubtitle.text = "${filtered.size} event(s) • team members"
        }
    }

    private fun fetchActivityLogs() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvActivityLog.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = teamLeadApiService.getTeamActivityLog()
                binding.progressBar.visibility = View.GONE

                if (response.success) {
                    allLogs = response.logs
                    // Apply current tab filter (default = All)
                    val currentTab = binding.tabLayout.selectedTabPosition
                    applyTabFilter(currentTab)
                } else {
                    Toast.makeText(
                        this@TeamLeadActivityLogActivity,
                        response.message ?: "Failed to fetch logs",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@TeamLeadActivityLogActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }
}
