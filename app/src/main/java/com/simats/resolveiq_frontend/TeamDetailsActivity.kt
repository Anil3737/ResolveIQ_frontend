package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.EmployeeAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.Employee
import com.simats.resolveiq_frontend.databinding.ActivityTeamDetailsBinding
import kotlinx.coroutines.launch

class TeamDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamDetailsBinding
    private val membersList = mutableListOf<Employee>()
    private lateinit var membersAdapter: EmployeeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val teamId = intent.getIntExtra("teamId", -1)
        val teamName = intent.getStringExtra("teamName") ?: "Team"
        val teamDesc = intent.getStringExtra("description") ?: ""
        val department = intent.getStringExtra("department") ?: ""
        val teamLead = intent.getStringExtra("teamLead") ?: ""
        val createdAt = intent.getStringExtra("createdAt") ?: ""

        setupUI(teamName, teamDesc, department, teamLead, createdAt)
        setupRecyclerView()
        loadTeamMembers(teamId)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnDeleteTeam.setOnClickListener {
            showDeleteConfirmation(teamId)
        }

        binding.btnUpdateTeam.setOnClickListener {
            showUpdateTeamDialog(teamId, teamName, teamDesc, department, teamLead)
        }
    }

    private fun showDeleteConfirmation(teamId: Int) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Delete Team")
            .setMessage("Are you sure you want to delete this team? This action cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                deleteTeam(teamId)
            }
            .show()
    }

    private fun deleteTeam(teamId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@TeamDetailsActivity).deleteTeam(teamId)
                binding.progressBar.visibility = View.GONE
                if (response.success) {
                    Toast.makeText(this@TeamDetailsActivity, "Team deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TeamDetailsActivity, response.message ?: "Failed to delete team", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@TeamDetailsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpdateTeamDialog(teamId: Int, name: String, desc: String, deptName: String, leadName: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val adminApi = RetrofitClient.getAdminApi(this@TeamDetailsActivity)
                
                // Fetch all data needed for update
                val deptsResp = adminApi.getDepartments()
                val leadsResp = adminApi.getUsers("TEAM_LEAD")
                val agentsResp = adminApi.getUsers("AGENT")

                binding.progressBar.visibility = View.GONE

                if (deptsResp.success && leadsResp.success && agentsResp.success) {
                    val deptId = deptsResp.data?.find { it.name.trim().equals(deptName.trim(), true) }?.id ?: 1
                    val leadId = leadsResp.data?.find { it.full_name?.trim().equals(leadName.trim(), true) }?.id ?: 0
                    
                    val allAgents = agentsResp.data?.filter { 
                        it.department_name?.trim().equals(deptName.trim(), true) == true 
                    } ?: emptyList()

                    if (allAgents.isEmpty()) {
                        Toast.makeText(this@TeamDetailsActivity, "No available agents for this department", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Multi-select dialog
                    val agentNames = allAgents.map { it.full_name ?: "Unknown" }.toTypedArray()
                    val checkedItems = BooleanArray(allAgents.size) { index ->
                        membersList.any { it.fullName == allAgents[index].full_name }
                    }
                    val selectedIds = mutableListOf<Int>()
                    allAgents.forEachIndexed { index, agent ->
                        if (checkedItems[index]) selectedIds.add(agent.id)
                    }

                    androidx.appcompat.app.AlertDialog.Builder(this@TeamDetailsActivity)
                        .setTitle("Update Team Agents")
                        .setMultiChoiceItems(agentNames, checkedItems) { _, which, isChecked ->
                            val agent = allAgents[which]
                            if (isChecked) {
                                if (!selectedIds.contains(agent.id)) selectedIds.add(agent.id)
                            } else {
                                selectedIds.remove(agent.id)
                            }
                        }
                        .setPositiveButton("Update") { _, _ ->
                            updateTeam(teamId, name, desc, deptId, leadId, selectedIds)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(this@TeamDetailsActivity, "Failed to load available staff for update", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@TeamDetailsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTeam(id: Int, name: String, desc: String, deptId: Int, leadId: Int, agentIds: List<Int>) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val request = com.simats.resolveiq_frontend.data.model.CreateTeamRequest(
                    name = name,
                    description = desc,
                    department_id = deptId,
                    team_lead_id = leadId,
                    agent_ids = agentIds
                )
                val response = RetrofitClient.getAdminApi(this@TeamDetailsActivity).updateTeam(id, request)
                binding.progressBar.visibility = View.GONE
                if (response.success) {
                    Toast.makeText(this@TeamDetailsActivity, "Team updated successfully", Toast.LENGTH_SHORT).show()
                    loadTeamMembers(id) // Refresh members list
                } else {
                    Toast.makeText(this@TeamDetailsActivity, response.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@TeamDetailsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI(name: String, desc: String, dept: String, lead: String, date: String) {
        binding.tvTeamName.text = name
        binding.tvDescription.text = if (desc.isNotEmpty()) desc else "No description provided."
        binding.tvIssueType.text = dept.uppercase()
        binding.tvTeamLead.text = lead
        binding.tvCreatedAt.text = date.take(10)
    }

    private fun setupRecyclerView() {
        membersAdapter = EmployeeAdapter(membersList) { employee ->
            // Optionally navigate to employee profile
        }
        binding.rvTeamMembers.apply {
            layoutManager = LinearLayoutManager(this@TeamDetailsActivity)
            adapter = membersAdapter
        }
    }

    private fun loadTeamMembers(teamId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Fetching all users and filtering by department (team association)
                val response = RetrofitClient.getAdminApi(this@TeamDetailsActivity).getTeamMembers(teamId)
                binding.progressBar.visibility = View.GONE
                
                if (response.success && response.data != null) {
                    membersList.clear()
                    val filteredUsers = response.data.filter { 
                        it.role.equals("AGENT", ignoreCase = true) 
                    }
                    
                    filteredUsers.forEach { user ->
                        membersList.add(
                            Employee(
                                employeeId = if (user.phone != null && user.phone.startsWith("EMP")) user.phone else "RIQ-${user.id.toString().padStart(4, '0')}",
                                fullName = user.full_name,
                                role = user.role,
                                department = user.department_name,
                                email = user.email,
                                location = user.location
                            )
                        )
                    }
                    
                    membersAdapter.notifyDataSetChanged()
                    binding.tvNoMembers.visibility = if (membersList.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@TeamDetailsActivity, "Failed to load members", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@TeamDetailsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
