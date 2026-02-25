package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityAgentProfileInfoBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class AgentProfileInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgentProfileInfoBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgentProfileInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        val api = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(api)

        binding.ivBack.setOnClickListener {
            finish()
        }

        loadProfileData()
    }

    private fun loadProfileData() {
        val intentFullName = intent.getStringExtra("fullName")
        if (intentFullName != null) {
            // Viewing another user's profile
            val intentRole = intent.getStringExtra("role") ?: "-"
            val intentEmpId = intent.getStringExtra("employeeId") ?: "-"
            val intentEmail = intent.getStringExtra("email") ?: "-"
            val intentLocation = intent.getStringExtra("location") ?: "Main Campus, SIMATS"
            val intentTeamLead = intent.getStringExtra("teamLead") ?: "-"
            val intentTeam = intent.getStringExtra("team") ?: "-"
            val intentJoinDate = intent.getStringExtra("joinDate") ?: "-"
            
            updateUI(intentFullName, intentRole, intentEmpId, intentEmail, intentLocation, intentTeamLead, intentTeam, intentJoinDate)
            return
        }

        // Default: Load own profile from preferences first
        val storedName = userPreferences.getUserName() ?: "-"
        val storedRole = userPreferences.getUserRole() ?: "-"
        val storedId = userPreferences.getUserId()
        val storedEmail = userPreferences.getUserEmail() ?: "-"
        val storedLocation = userPreferences.getUserLocation() ?: "Main Campus, SIMATS"
        
        updateUI(
            storedName, 
            storedRole, 
            if (storedId != -1) "RIQ-${storedId.toString().padStart(4, '0')}" else "-", 
            storedEmail, 
            storedLocation,
            "-", "-", "-"
        )

        lifecycleScope.launch {
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                val user = result.getOrNull()
                user?.let {
                    val empId = if (it.phone != null && it.phone.startsWith("EMP")) it.phone else "RIQ-${it.id.toString().padStart(4, '0')}"
                    updateUI(
                        it.full_name, 
                        it.role, 
                        empId, 
                        it.email, 
                        it.location ?: "Main Campus, SIMATS",
                        it.team_lead_name ?: "Santhosh Mani", // Dummy for now if not in DB
                        it.department_name ?: "Network Automation", // Mapping department to Team
                        it.joining_date ?: "12 Jan 2024" // Dummy for now
                    )
                    
                    // Sync preferences
                    userPreferences.saveUserName(it.full_name)
                    userPreferences.saveUserRole(it.role)
                    userPreferences.saveUserEmail(it.email)
                    userPreferences.saveUserLocation(it.location)
                }
            }
        }
    }

    private fun updateUI(
        name: String, 
        role: String, 
        empId: String, 
        email: String, 
        location: String,
        teamLead: String,
        team: String,
        joinDate: String
    ) {
        binding.tvDispName.text = name
        binding.tvDispRole.text = role.lowercase().replaceFirstChar { it.uppercase() }
        
        binding.tvFullName.text = name
        binding.tvRole.text = role.lowercase().replaceFirstChar { it.uppercase() }
        binding.tvEmpId.text = empId
        binding.tvEmail.text = if (email != "-") email else "-"
        binding.tvLocation.text = location
        
        binding.tvTeamLead.text = teamLead
        binding.tvTeam.text = team
        binding.tvJoinDate.text = joinDate
    }
}
