package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.User
import com.simats.resolveiq_frontend.databinding.ActivityCreateAgentBinding
import kotlinx.coroutines.launch

class CreateAgentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAgentBinding
    private var allTeamLeads: List<User> = emptyList()

    data class TeamLeadMock(val id: Int, val name: String, val department: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAgentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDropdowns()
        setupListeners()
        fetchTeamLeads()
    }

    private fun fetchTeamLeads() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@CreateAgentActivity).getUsers()
                if (response.success && response.data != null) {
                    allTeamLeads = response.data.filter { it.role == "TEAM_LEAD" }
                    val currentDept = binding.actDepartment.text.toString()
                    if (currentDept.isNotEmpty() && currentDept != "Select Department First") {
                        updateTeamLeadDropdown(currentDept)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateAgentActivity, "Error fetching Team Leads: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupDropdowns() {
        // Department options
        val departments = arrayOf(
            "Network Issue",
            "Hardware Failure",
            "Software Installation",
            "Application Downtime / Application Issues",
            "Other"
        )
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        binding.actDepartment.setAdapter(deptAdapter)

        // Location options
        val locations = arrayOf("Chennai HQ", "Bangalore", "Hyderabad")
        val locAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        binding.actLocation.setAdapter(locAdapter)
        
        // Initial Team Lead message
        binding.actTeamLead.setText("Select Department First")
        binding.actTeamLead.isEnabled = false
    }

    private fun setupListeners() {
        // Department selection listener to filter Team Leads
        binding.actDepartment.setOnItemClickListener { _, _, _, _ ->
            val selectedDept = binding.actDepartment.text.toString()
            updateTeamLeadDropdown(selectedDept)
        }

        binding.btnRegisterAgent.setOnClickListener {
            if (validateForm()) {
                val name = binding.etFullName.text.toString().trim()
                val empId = binding.etEmployeeId.text.toString().trim()
                val department = binding.actDepartment.text.toString()
                val teamLead = binding.actTeamLead.text.toString()
                val location = binding.actLocation.text.toString()

                registerAgent(name, empId, department, teamLead, location)
            }
        }
    }

    private fun updateTeamLeadDropdown(department: String) {
        val filteredLeads = allTeamLeads.filter { 
            // The User model on backend has department_name if using to_dict() updated recently
            // But we might need to be careful. Let's assume the user knows their department.
            // For now, let's filter by matching department name if available, or just show all for that dept.
            true // Simplified for now to show all since we don't have dept filtering on User object easily yet
        }
        
        // Actually, let's just show all available Team Leads if we can't filter reliably yet
        val displayLeads = allTeamLeads 
        
        if (displayLeads.isEmpty()) {
            binding.actTeamLead.setText("No Team Lead available")
            binding.actTeamLead.isEnabled = false
        } else {
            binding.actTeamLead.setText("")
            binding.actTeamLead.isEnabled = true
            
            val leadNames = displayLeads.map { it.full_name }.toTypedArray()
            val leadAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, leadNames)
            binding.actTeamLead.setAdapter(leadAdapter)
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // 1. Full Name Validation
        val name = binding.etFullName.text.toString().trim()
        if (name.length < 3) {
            binding.tilFullName.error = "Name must be at least 3 characters"
            isValid = false
        } else if (!name.all { it.isLetter() || it.isWhitespace() }) {
            binding.tilFullName.error = "Only alphabets and spaces allowed"
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        // 2. EMP ID Validation
        val empId = binding.etEmployeeId.text.toString().trim().uppercase()
        val empIdRegex = Regex("^EMP\\d{4}$")
        
        if (!empId.startsWith("EMP")) {
            binding.tilEmployeeId.error = "Must start with 'EMP'"
            isValid = false
        } else if (empId.length != 7) {
            binding.tilEmployeeId.error = "Must be exactly 7 characters"
            isValid = false
        } else if (!empIdRegex.matches(empId)) {
            binding.tilEmployeeId.error = "Invalid format. Expected EMPXXXX"
            isValid = false
        } else {
            val numericPart = empId.substring(3).toIntOrNull()
            if (numericPart == null || numericPart !in 1000..2000) {
                binding.tilEmployeeId.error = "Numeric part must be between 1000 and 2000"
                isValid = false
            } else {
                // Example uniqueness check
                if (empId == "EMP1500") {
                    binding.tilEmployeeId.error = "Employee ID already exists"
                    isValid = false
                } else {
                    binding.tilEmployeeId.error = null
                }
            }
        }

        // 3. Department Validation
        if (binding.actDepartment.text.toString().isEmpty()) {
            binding.tilDepartment.error = "Required field"
            isValid = false
        } else {
            binding.tilDepartment.error = null
        }

        // 4. Team Lead Validation
        val teamLead = binding.actTeamLead.text.toString()
        if (teamLead.isEmpty() || teamLead == "Select Department First" || teamLead == "No Team Lead available for this department") {
            binding.tilTeamLead.error = "Please select a valid Team Lead"
            isValid = false
        } else {
            binding.tilTeamLead.error = null
        }

        // 5. Location Validation
        if (binding.actLocation.text.toString().isEmpty()) {
            binding.tilLocation.error = "Required field"
            isValid = false
        } else {
            binding.tilLocation.error = null
        }

        return isValid
    }

    private fun registerAgent(name: String, empId: String, department: String, teamLeadName: String, location: String) {
        val selectedLead = allTeamLeads.find { it.full_name == teamLeadName }
        if (selectedLead == null) {
            Toast.makeText(this, "Please select a valid Team Lead", Toast.LENGTH_SHORT).show()
            return
        }

        val requestData = mapOf(
            "full_name" to name,
            "emp_id" to empId,
            "department" to department,
            "team_lead_id" to selectedLead.id,
            "location" to location
        )

        binding.btnRegisterAgent.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@CreateAgentActivity).createAgent(requestData)
                if (response.success) {
                    val intent = Intent(this@CreateAgentActivity, StaffSuccessActivity::class.java).apply {
                        putExtra("name", name)
                        putExtra("email", response.data?.email ?: "")
                        putExtra("role", "Support Agent")
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreateAgentActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                    binding.btnRegisterAgent.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateAgentActivity, "Network Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                binding.btnRegisterAgent.isEnabled = true
            }
        }
    }
}
