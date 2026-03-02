package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityChangePasswordBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authApi = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(authApi)

        setupListeners()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnConfirmChange.setOnClickListener {
            performChangePassword()
        }
    }

    private fun performChangePassword() {
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "Password is required"
            return
        }
        if (newPassword.length < 8) {
            binding.tilNewPassword.error = "Password must be at least 8 characters"
            return
        }
        binding.tilNewPassword.error = null

        if (confirmPassword != newPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            return
        }
        binding.tilConfirmPassword.error = null

        // Show loading state
        binding.btnConfirmChange.isEnabled = false
        binding.btnConfirmChange.text = "Updating..."

        lifecycleScope.launch {
            val result = authRepository.changePassword(newPassword)
            if (result.isSuccess) {
                // Success - navigate to success screen
                val intent = Intent(this@ChangePasswordActivity, PasswordSuccessActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                binding.btnConfirmChange.isEnabled = true
                binding.btnConfirmChange.text = "Confirm Change Password"
                val error = result.exceptionOrNull()?.message ?: "Failed to update password"
                Toast.makeText(this@ChangePasswordActivity, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}
