package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.Team
import com.simats.resolveiq_frontend.adapter.TeamAdapter
import com.simats.resolveiq_frontend.databinding.ActivityTeamsBinding

class TeamsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        
        binding.btnCreateTeam.setOnClickListener {
            val intent = android.content.Intent(this, CreateTeamActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val dummyTeams = listOf(
            Team(1, "Network Team", "CONNECTIVITY", "98% SLA", "On Track", "Sarah Chen", 
                R.drawable.ic_network_node, getColor(R.color.blue_50), getColor(R.color.green_600), R.drawable.bg_status_new),
            Team(2, "Hardware Team", "INFRASTRUCTURE", "84% SLA", "Average", "Marcus Vane", 
                R.drawable.ic_wrench, getColor(R.color.purple_50), getColor(R.color.blue_600), R.drawable.bg_admin_menu_selected),
            Team(3, "Security Team", "COMPLIANCE", "79% SLA", "Action Required", "David Wu", 
                R.drawable.ic_shield_small, getColor(R.color.orange_50), getColor(R.color.orange_500), R.drawable.bg_status_medium),
            Team(4, "Software Team", "DEPLOYMENT", "92% SLA", "Excellent", "Elena Rodriguez", 
                R.drawable.ic_grid, getColor(R.color.green_50), getColor(R.color.green_600), R.drawable.bg_status_new),
            Team(5, "Support Team", "INCIDENT MGMT", "88% SLA", "Stable", "James Smith", 
                R.drawable.ic_chat, getColor(R.color.cyan_50), getColor(R.color.blue_600), R.drawable.bg_admin_menu_selected),
            Team(6, "Database Team", "PERFORMANCE", "95% SLA", "Optimized", "Anna Lee", 
                R.drawable.ic_bar_chart, getColor(R.color.rose_50), getColor(R.color.green_600), R.drawable.bg_status_new)
        )

        binding.rvTeams.layoutManager = LinearLayoutManager(this)
        binding.rvTeams.adapter = TeamAdapter(dummyTeams)
    }
}
