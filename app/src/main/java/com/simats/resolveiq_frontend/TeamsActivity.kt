package com.simats.resolveiq_frontend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.TeamAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityTeamsBinding
import kotlinx.coroutines.launch

class TeamsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamsBinding
    private lateinit var teamAdapter: TeamAdapter
    private var allTeams: List<com.simats.resolveiq_frontend.data.model.TeamData> = emptyList()
    private var currentFilter: String = "All Teams"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFilters()
        loadTeamsFromApi()

        binding.btnCreateTeam.setOnClickListener {
            val intent = android.content.Intent(this, CreateTeamActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTeamsFromApi()
    }

    private fun setupRecyclerView() {
        teamAdapter = TeamAdapter(emptyList()) { team ->
            navigateToTeamDetails(team)
        }
        binding.rvTeams.layoutManager = LinearLayoutManager(this)
        binding.rvTeams.adapter = teamAdapter
    }

    private fun setupFilters() {
        binding.filterAll.setOnClickListener { updateFilter("All Teams") }
        binding.filterNetwork.setOnClickListener { updateFilter("Network") }
        binding.filterHardware.setOnClickListener { updateFilter("Hardware") }
        binding.filterSoftware.setOnClickListener { updateFilter("Software") }
        binding.filterApplication.setOnClickListener { updateFilter("Application") }
        binding.filterOthers.setOnClickListener { updateFilter("Others") }
    }

    private fun updateFilter(filter: String) {
        currentFilter = filter
        applyFilter()
        updateFilterUI()
    }

    private fun applyFilter() {
        val filteredList = if (currentFilter == "All Teams") {
            allTeams
        } else if (currentFilter == "Others") {
            // "Others" includes anything not in the main categories
            val mainCategories = listOf("Network", "Hardware", "Software", "Application")
            allTeams.filter { team ->
                mainCategories.none { cat -> team.department.contains(cat, ignoreCase = true) }
            }
        } else {
            allTeams.filter { team ->
                // Use contains for more flexible matching (e.g., "NETWORK ISSUE" matches "Network")
                team.department.contains(currentFilter, ignoreCase = true)
            }
        }
        android.util.Log.d("TeamsActivity", "Filter: $currentFilter, Count: ${filteredList.size}")
        teamAdapter.updateData(filteredList)
    }

    private fun updateFilterUI() {
        val filters = listOf(
            binding.filterAll, binding.filterNetwork, binding.filterHardware,
            binding.filterSoftware, binding.filterApplication, binding.filterOthers
        )

        filters.forEach { textView ->
            if (textView.text.toString().equals(currentFilter, ignoreCase = true)) {
                textView.setBackgroundResource(R.drawable.button_primary)
                textView.setTextColor(getColor(R.color.white))
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                textView.setBackgroundResource(R.drawable.bg_card_border)
                textView.setTextColor(getColor(R.color.text_secondary))
                textView.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun navigateToTeamDetails(team: com.simats.resolveiq_frontend.data.model.TeamData) {
        val intent = android.content.Intent(this, TeamDetailsActivity::class.java)
        intent.putExtra("teamId", team.id)
        intent.putExtra("teamName", team.name)
        intent.putExtra("description", team.description)
        intent.putExtra("department", team.department)
        intent.putExtra("teamLead", team.team_lead)
        intent.putExtra("createdAt", team.created_at)
        startActivity(intent)
    }

    private fun loadTeamsFromApi() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@TeamsActivity).getTeams()
                if (response.success && response.data != null) {
                    allTeams = response.data
                    applyFilter() // Apply current filter to new data
                    if (response.data.isEmpty()) {
                        Toast.makeText(this@TeamsActivity, "No teams created yet", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@TeamsActivity, "Failed to load teams: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TeamsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
