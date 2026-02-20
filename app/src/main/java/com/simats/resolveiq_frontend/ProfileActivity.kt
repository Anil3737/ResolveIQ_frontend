package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityProfileBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        val api = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(api)

        setupListeners()
        loadUserData()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        // Placeholders for other menu items
        binding.btnViewTickets.setOnClickListener {
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }
        
        binding.btnTicketProgress.setOnClickListener {
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }
        
        binding.btnWorkflow.setOnClickListener {
            startActivity(Intent(this, WorkflowActivity::class.java))
        }
    }

    private fun loadUserData() {
        // Show initial data from preferences
        val storedName = userPreferences.getUserName() ?: "Employee"
        val storedRole = userPreferences.getUserRole() ?: "Premium AI Support Member"
        
        binding.tvProfileName.text = storedName
        binding.tvProfileMemberStatus.text = storedRole.lowercase().capitalize()

        lifecycleScope.launch {
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                val user = result.getOrNull()
                user?.let {
                    binding.tvProfileName.text = it.full_name
                    binding.tvProfileMemberStatus.text = it.role.lowercase().capitalize()
                    
                    // Sync preferences
                    userPreferences.saveUserName(it.full_name)
                    userPreferences.saveUserRole(it.role)
                }
            }
        }
    }

    private fun performLogout() {
        userPreferences.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
