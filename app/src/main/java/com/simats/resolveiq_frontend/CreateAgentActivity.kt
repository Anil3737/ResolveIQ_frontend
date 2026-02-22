package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityCreateAgentBinding

class CreateAgentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAgentBinding
    
    // Mock data for Team Leads
    private val teamLeads = listOf(
        TeamLead(1, "John Smith", "Network Issue"),
        TeamLead(2, "Sarah Johnson", "Hardware Failure"),
        TeamLead(3, "Robert Williams", "Network Issue"),
        TeamLead(4, "Michael Brown", "Software Installation"),
        TeamLead(5, "David Jones", "Application Downtime / Application Issues"),
        TeamLead(6, "Emily Davis", "Other"),
        TeamLead(7, "Chris Wilson", "Software Installation")
    )

    data class TeamLead(val id: Int, val name: String, val department: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAgentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDropdowns()
        setupListeners()
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
        val filteredLeads = teamLeads.filter { it.department == department }
        
        if (filteredLeads.isEmpty()) {
            binding.actTeamLead.setText("No Team Lead available for this department")
            binding.actTeamLead.isEnabled = false
            binding.tilTeamLead.error = "No Team Lead available"
        } else {
            binding.actTeamLead.setText("")
            binding.actTeamLead.isEnabled = true
            binding.tilTeamLead.error = null
            
            val leadNames = filteredLeads.map { it.name }.toTypedArray()
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

    private fun registerAgent(name: String, empId: String, department: String, teamLead: String, location: String) {
        // Simulation of backend registration
        Toast.makeText(this, "Support Agent Registered: $name", Toast.LENGTH_LONG).show()
        
        // Redirect to Admin Users Page
        val intent = Intent(this, UsersListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
