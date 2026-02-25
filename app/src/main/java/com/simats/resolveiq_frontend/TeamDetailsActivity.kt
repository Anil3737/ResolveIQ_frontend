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
