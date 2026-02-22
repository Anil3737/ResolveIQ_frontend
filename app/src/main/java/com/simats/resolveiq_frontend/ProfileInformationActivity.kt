package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityProfileInformationBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class ProfileInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInformationBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        val authApi = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(authApi)

        setupListeners()
        fetchUserProfile()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserProfile() {
        setLoading(true)
        lifecycleScope.launch {
            val result = authRepository.getCurrentUser()
            setLoading(false)
            
            if (result.isSuccess) {
                val user = result.getOrThrow()
                // Update SharedPreferences in background
                userPreferences.saveUserName(user.full_name)
                userPreferences.saveUserRole(user.role)
                userPreferences.saveUserEmail(user.email)
                userPreferences.saveUserLocation(user.location)
                userPreferences.saveUserPhone(user.phone)
                
                bindUserData(user.full_name, user.role, user.phone, user.location, user.email)
            } else {
                val exception = result.exceptionOrNull()
                val message = exception?.message ?: "Failed to load profile"
                Toast.makeText(this@ProfileInformationActivity, message, Toast.LENGTH_LONG).show()
                
                if (message.contains("unauthorized", ignoreCase = true) || message.contains("token", ignoreCase = true)) {
                    handleLogout()
                }
            }
        }
    }

    private fun bindUserData(name: String, role: String, employeeId: String?, location: String?, email: String) {
        binding.tvFullName.text = name
        binding.tvHeaderName.text = name
        
        binding.tvRole.text = role.replaceFirstChar { it.uppercase() }
        binding.tvHeaderRole.text = role.replaceFirstChar { it.uppercase() }
        
        binding.tvEmployeeId.text = employeeId ?: "N/A"
        binding.tvLocation.text = location ?: "N/A"
        binding.tvEmail.text = email

        // Initials
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        
        binding.tvInitials.text = if (initials.isNotEmpty()) initials else "?"
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun handleLogout() {
        userPreferences.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
