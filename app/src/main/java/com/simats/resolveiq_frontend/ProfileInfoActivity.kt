package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityProfileInfoBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class ProfileInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInfoBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInfoBinding.inflate(layoutInflater)
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
        // Initial data from preferences
        val storedName = userPreferences.getUserName() ?: "-"
        val storedRole = userPreferences.getUserRole() ?: "-"
        val storedId = userPreferences.getUserId()
        
        updateUI(storedName, storedRole, storedId, "-")

        lifecycleScope.launch {
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                val user = result.getOrNull()
                user?.let {
                    updateUI(it.full_name, it.role, it.id, it.email)
                    
                    // Sync preferences if needed
                    userPreferences.saveUserName(it.full_name)
                    userPreferences.saveUserRole(it.role)
                }
            }
        }
    }

    private fun updateUI(name: String, role: String, id: Int, email: String) {
        binding.tvDispName.text = name
        binding.tvDispRole.text = role.lowercase().replaceFirstChar { it.uppercase() }
        
        binding.tvFullName.text = name
        binding.tvRole.text = role.lowercase().replaceFirstChar { it.uppercase() }
        binding.tvEmpId.text = if (id != -1) "RIQ-${id.toString().padStart(4, '0')}" else "-"
        binding.tvEmail.text = if (email != "-") email else "-"
    }
}
