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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadTeamsFromApi()

        binding.btnCreateTeam.setOnClickListener {
            val intent = android.content.Intent(this, CreateTeamActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload teams whenever we return to this screen (e.g., after creating a team)
        loadTeamsFromApi()
    }

    private fun setupRecyclerView() {
        teamAdapter = TeamAdapter(emptyList()) { team ->
            navigateToTeamDetails(team)
        }
        binding.rvTeams.layoutManager = LinearLayoutManager(this)
        binding.rvTeams.adapter = teamAdapter
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
                    // Replace adapter's data by creating a new adapter
                    binding.rvTeams.adapter = TeamAdapter(response.data) { team ->
                        navigateToTeamDetails(team)
                    }
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
