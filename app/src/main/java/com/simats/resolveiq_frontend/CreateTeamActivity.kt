package com.simats.resolveiq_frontend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.Department
import com.simats.resolveiq_frontend.data.model.User
import com.simats.resolveiq_frontend.databinding.ActivityCreateTeamBinding
import kotlinx.coroutines.launch
import android.content.Intent
import android.view.View


class CreateTeamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTeamBinding
    private var selectedTeamLeadId: Int? = null
    private var teamLeads: List<User> = emptyList()
    private var supportAgents: List<User> = emptyList()
    private var departments: List<Department> = emptyList()
    private val selectedAgentIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        fetchTeamLeads()
        fetchSupportAgents()
        fetchDepartments()
    }

    private fun fetchDepartments() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@CreateTeamActivity).getDepartments()
                if (response.success && response.data != null) {
                    departments = response.data
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    private fun fetchSupportAgents() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@CreateTeamActivity).getUsers("AGENT")
                if (response.success && response.data != null) {
                    supportAgents = response.data
                }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    private fun fetchTeamLeads() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@CreateTeamActivity).getUsers("TEAM_LEAD")
                if (response.success && response.data != null) {
                    teamLeads = response.data
                } else {
                    Toast.makeText(this@CreateTeamActivity, "Could not load Team Leads", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateTeamActivity, "Error loading Team Leads: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvSelectSpecialty.setOnClickListener {
            showIssueTypePicker()
        }

        binding.rlSelectTeamLead.setOnClickListener {
            showTeamLeadPicker()
        }

        binding.rlSelectAgents.setOnClickListener {
            showAgentPicker()
        }

        binding.btnSubmitCreateTeam.setOnClickListener {
            val teamName = binding.etTeamName.text.toString().trim()
            val specialty = binding.tvSelectSpecialty.text.toString()
            val description = binding.etTeamDescription.text.toString().trim()
            
            if (teamName.isEmpty()) {
                Toast.makeText(this, "Please enter team name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (specialty == "Select specialty") {
                Toast.makeText(this, "Please select an issue type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTeamLeadId == null) {
                Toast.makeText(this, "Please select a team lead", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val department = departments.find { it.name.trim().equals(specialty.trim(), ignoreCase = true) }
            val departmentId = department?.id ?: 1 // Fallback to 1 if not found
            
            lifecycleScope.launch {
                try {
                    val request = com.simats.resolveiq_frontend.data.model.CreateTeamRequest(
                        name = teamName,
                        description = description,
                        department_id = departmentId,
                        team_lead_id = selectedTeamLeadId!!,
                        agent_ids = selectedAgentIds
                    )
                    
                    val response = RetrofitClient.getAdminApi(this@CreateTeamActivity).createTeam(request)
                    if (response.success) {
                        Toast.makeText(this@CreateTeamActivity, "Team Created Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateTeamActivity, "Failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CreateTeamActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showIssueTypePicker() {
        val specialties = arrayOf(
            "Network Issue",
            "Hardware Failure",
            "Software Installation",
            "Application Downtime / Application Issues",
            "Other"
        )
        
        val descriptions = mapOf(
            "Network Issue" to "Handles LAN/WiFi outages, VPN connectivity problems, internet downtime, firewall access, and internal network disruptions. Ensures stable, secure, and high-availability network infrastructure across all office locations.",
            "Hardware Failure" to "Manages laptop/desktop malfunctions, printer issues, peripheral failures, hardware diagnostics, and device replacements. Responsible for asset tracking, warranty coordination, and preventive hardware maintenance.",
            "Software Installation" to "Processes software installation requests, license activation, version upgrades, patch deployment, and tool configuration. Ensures compliance with organizational security policies and software standards.",
            "Application Downtime / Application Issues" to "Handles production outages, portal errors (500/503), performance degradation, access issues, and application-level bugs. Coordinates with server/database teams to restore services within SLA timelines.",
            "Other" to "Handles miscellaneous and cross-functional IT issues not classified under primary categories. Includes email configuration issues, account access requests, minor configuration problems, general system assistance, and user guidance."
        )

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Specialty")
        builder.setItems(specialties) { _, which ->
            val selected = specialties[which]
            binding.tvSelectSpecialty.text = selected
            
            // Auto-populate description and move cursor to end
            val defaultDesc = descriptions[selected] ?: ""
            binding.etTeamDescription.setText(defaultDesc)
            binding.etTeamDescription.setSelection(defaultDesc.length)

            // Clear selected agents when department changes
            selectedAgentIds.clear()
            binding.tvSelectedAgents.text = "Select support agents"

            // Clear selected team lead when department changes
            selectedTeamLeadId = null
            binding.tvSelectTeamLead.text = "Select team lead"
        }
        builder.show()
    }

    private fun showTeamLeadPicker() {
        val specialty = binding.tvSelectSpecialty.text.toString()
        if (specialty == "Select specialty") {
            Toast.makeText(this, "Please select an issue type first", Toast.LENGTH_SHORT).show()
            return
        }

        if (teamLeads.isEmpty()) {
            Toast.makeText(this, "No Team Leads found. Please create a Team Lead first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Filter Team Leads by the selected specialty
        val filteredLeads = teamLeads.filter { 
            it.department_name?.trim().equals(specialty.trim(), ignoreCase = true) == true
        }

        if (filteredLeads.isEmpty()) {
            Toast.makeText(this, "No Team Leads available for $specialty", Toast.LENGTH_SHORT).show()
            return
        }

        val names = filteredLeads.map { it.full_name }.toTypedArray()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Team Lead ($specialty)")
        builder.setItems(names) { _, which ->
            val selectedLead = filteredLeads[which]
            binding.tvSelectTeamLead.text = selectedLead.full_name
            selectedTeamLeadId = selectedLead.id
        }
        builder.show()
    }

    private fun showAgentPicker() {
        val specialty = binding.tvSelectSpecialty.text.toString()
        if (specialty == "Select specialty") {
            Toast.makeText(this, "Please select an issue type first", Toast.LENGTH_SHORT).show()
            return
        }

        val filteredAgents = supportAgents.filter { 
            it.department_name?.trim().equals(specialty.trim(), ignoreCase = true) == true
        }

        if (filteredAgents.isEmpty()) {
            Toast.makeText(this, "No support agents available for this specialty", Toast.LENGTH_SHORT).show()
            return
        }

        val names = filteredAgents.map { it.full_name }.toTypedArray()
        val checkedItems = BooleanArray(filteredAgents.size) { index ->
            selectedAgentIds.contains(filteredAgents[index].id)
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Support Agents")
        builder.setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
            val agent = filteredAgents[which]
            if (isChecked) {
                if (!selectedAgentIds.contains(agent.id)) {
                    selectedAgentIds.add(agent.id)
                }
            } else {
                selectedAgentIds.remove(agent.id)
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            if (selectedAgentIds.isEmpty()) {
                binding.tvSelectedAgents.text = "Select support agents"
            } else {
                val selectedNames = supportAgents.filter { selectedAgentIds.contains(it.id) }.map { it.full_name }
                binding.tvSelectedAgents.text = selectedNames.joinToString(", ")
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
