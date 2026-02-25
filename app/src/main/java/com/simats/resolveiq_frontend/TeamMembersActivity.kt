package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.TeamMembersAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityTeamMembersBinding
import com.simats.resolveiq_frontend.repository.TeamLeadRepository
import kotlinx.coroutines.launch

class TeamMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamMembersBinding
    private lateinit var adapter: TeamMembersAdapter
    private lateinit var repository: TeamLeadRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupRepository()
        loadTeamMembers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = TeamMembersAdapter(this)
        binding.rvTeamMembers.layoutManager = LinearLayoutManager(this)
        binding.rvTeamMembers.adapter = adapter
    }

    private fun setupRepository() {
        val apiService = RetrofitClient.getTeamLeadApi(this)
        repository = TeamLeadRepository(apiService)
    }

    private fun loadTeamMembers() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val members = repository.getTeamMembers()
                binding.progressBar.visibility = View.GONE
                
                if (members.isNotEmpty()) {
                    adapter.setMembers(members)
                    binding.rvTeamMembers.visibility = View.VISIBLE
                } else {
                    binding.rvTeamMembers.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@TeamMembersActivity, "Failed to load team members: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
