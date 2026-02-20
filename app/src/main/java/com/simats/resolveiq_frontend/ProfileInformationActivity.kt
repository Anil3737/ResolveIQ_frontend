package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityProfileInformationBinding
import com.simats.resolveiq_frontend.utils.UserPreferences

class ProfileInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInformationBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        setupListeners()
        loadUserData()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val fullName = userPreferences.getUserName() ?: "N/A"
        val role = userPreferences.getUserRole() ?: "N/A"
        val phone = userPreferences.getUserPhone() ?: "N/A" // Treating Phone as Employee ID
        val location = userPreferences.getUserLocation() ?: "N/A"
        val email = userPreferences.getUserEmail() ?: "N/A"

        binding.tvFullName.text = fullName
        binding.tvRole.text = role.replaceFirstChar { it.uppercase() }
        binding.tvEmployeeId.text = phone
        binding.tvLocation.text = location
        binding.tvEmail.text = email

        // simple initials
        val initials = fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        
        binding.tvInitials.text = if (initials.isNotEmpty()) initials else "?"
    }
}
