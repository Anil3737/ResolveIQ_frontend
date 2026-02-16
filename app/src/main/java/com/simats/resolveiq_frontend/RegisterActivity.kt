package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityRegisterBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.data.model.RegisterRequest
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authRepository: AuthRepository
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize dependencies
        val api = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(api)

        setupListeners()
    }

    private fun setupListeners() {
        // Sign In Link
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Password Visibility Toggle
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(binding.etPassword, isPasswordVisible, binding.ivPasswordToggle)
        }

        // Confirm Password Visibility Toggle
        binding.ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(binding.etConfirmPassword, isConfirmPasswordVisible, binding.ivConfirmPasswordToggle)
        }

        // Create Account Button
        binding.btnCreateAccountSubmit.setOnClickListener {
            handleRegistration()
        }
    }

    private fun togglePasswordVisibility(editText: android.widget.EditText, isVisible: Boolean, toggleIcon: android.widget.ImageView) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_visibility) // Corrected icon name check later or assume generic
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(editText.text.length)
    }

    private fun handleRegistration() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etCompanyEmail.text.toString().trim()
        val phone = binding.etEmployeeId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to the Terms of Service", Toast.LENGTH_SHORT).show()
            return
        }

        // Set loading state
        setLoading(true)

        lifecycleScope.launch {
            val request = RegisterRequest(
                full_name = fullName,
                email = email,
                phone = phone,
                password = password,
                department_id = 1 // Default to 1
            )
            
            val result = authRepository.register(request)
            
            setLoading(false)
            
            if (result.isSuccess) {
                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            } else {
                val e = result.exceptionOrNull()
                val errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "Connection timeout. Please check your internet."
                    is java.net.ConnectException -> "Could not connect to server. Ensure backend is running."
                    else -> "Registration failed: ${e?.localizedMessage ?: "Unknown error"}"
                }
                Log.e("RegisterActivity", "Registration Error", e)
                showErrorDialog("Error", errorMessage)
            }
        }
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnCreateAccountSubmit.isEnabled = !isLoading
        binding.btnCreateAccountSubmit.text = if (isLoading) "" else getString(R.string.create_account)
        binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }
}
