package com.simats.resolveiq_frontend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.User
import com.simats.resolveiq_frontend.databinding.ActivityCreateTeamBinding
import kotlinx.coroutines.launch

data class TeamLead(val id: Int, val name: String)

class CreateTeamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTeamBinding
    private var selectedTeamLeadId: Int? = null
    private var teamLeads: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        fetchTeamLeads()
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

            val departmentId = getDepartmentId(specialty)
            
            val message = """
                Team Created!
                Name: $teamName
                Specialty: $specialty (Dept ID: $departmentId)
                Description: $description
                Lead ID: $selectedTeamLeadId
            """.trimIndent()

            // Backend part will be done later
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun getDepartmentId(specialty: String): Int {
        return when (specialty) {
            "Network Issue" -> 1
            "Hardware Failure" -> 2
            "Software Installation" -> 3
            "Application Downtime / Application Issues" -> 4
            else -> 5 // Other
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
        }
        builder.show()
    }

    private fun showTeamLeadPicker() {
        if (teamLeads.isEmpty()) {
            Toast.makeText(this, "No Team Leads found. Please create a Team Lead first.", Toast.LENGTH_SHORT).show()
            return
        }

        val names = teamLeads.map { it.full_name }.toTypedArray()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Team Lead")
        builder.setItems(names) { _, which ->
            val selectedLead = teamLeads[which]
            binding.tvSelectTeamLead.text = selectedLead.full_name
            selectedTeamLeadId = selectedLead.id
        }
        builder.show()
    }
}
