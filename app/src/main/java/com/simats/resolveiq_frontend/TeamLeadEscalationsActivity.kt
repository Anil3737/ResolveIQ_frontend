package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.TicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.api.TeamLeadApiService
import com.simats.resolveiq_frontend.databinding.ActivityTeamLeadEscalationsBinding
import kotlinx.coroutines.launch

class TeamLeadEscalationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamLeadEscalationsBinding
    private lateinit var teamLeadApiService: TeamLeadApiService
    private lateinit var adapter: TicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamLeadEscalationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teamLeadApiService = RetrofitClient.getTeamLeadApi(this)
        setupUI()
        fetchEscalations()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivRefresh.setOnClickListener { fetchEscalations() }

        adapter = TicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, TicketDetailsActivity::class.java).apply {
                putExtra("ticket_id", ticket.id)
            }
            startActivity(intent)
        }

        binding.rvEscalations.apply {
            layoutManager = LinearLayoutManager(this@TeamLeadEscalationsActivity)
            adapter = this@TeamLeadEscalationsActivity.adapter
        }
    }

    private fun fetchEscalations() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvEscalations.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = teamLeadApiService.getTeamTickets()
                binding.progressBar.visibility = View.GONE

                if (response.success) {
                    val allTickets = response.data ?: emptyList()
                    // Filter to only ESCALATED tickets from team's department
                    val escalatedTickets = allTickets.filter { it.status == "ESCALATED" }

                    // Compute summary counts
                    val totalEscalated = escalatedTickets.size
                    val slaBreachedCount = escalatedTickets.count { it.sla_breached == true }
                    val highPriorityCount = escalatedTickets.count {
                        it.priority.uppercase() in listOf("P1", "HIGH")
                    }

                    binding.tvTotalEscalated.text = totalEscalated.toString()
                    binding.tvSlaBreached.text = slaBreachedCount.toString()
                    binding.tvHighPriority.text = highPriorityCount.toString()
                    binding.tvEscalationCount.text = "$totalEscalated ticket(s) requiring attention"

                    if (escalatedTickets.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        adapter.updateTickets(escalatedTickets)
                        binding.rvEscalations.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@TeamLeadEscalationsActivity,
                        response.message ?: "Failed to load escalations",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@TeamLeadEscalationsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.layoutEmpty.visibility = View.VISIBLE
            }
        }
    }
}
