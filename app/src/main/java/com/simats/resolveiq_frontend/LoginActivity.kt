package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePasswordVisibility: ImageView
    private lateinit var forgotPasswordText: TextView
    private lateinit var signInButton: Button
    
    private lateinit var authRepository: AuthRepository
    private lateinit var userPreferences: UserPreferences
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeDependencies()
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        signInButton = findViewById(R.id.signInButton)
        
        // Setup Account Buttons
        findViewById<Button>(R.id.btnCreateAccount).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun initializeDependencies() {
        val authApi = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(authApi)
        userPreferences = UserPreferences(this)
    }

    private fun setupClickListeners() {
        signInButton.setOnClickListener {
            performLogin()
        }

        togglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }

        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            val result = authRepository.login(email, password)
            setLoading(false)
            
            if (result.isSuccess) {
                val response = result.getOrThrow()
                val data = response.data
                
                if (data != null) {
                    // Save to Preferences
                    userPreferences.saveToken(data.access_token)
                    userPreferences.saveUserId(data.user.id)
                    userPreferences.saveUserRole(data.user.role)
                    userPreferences.saveUserName(data.user.full_name)
                    userPreferences.saveUserEmail(data.user.email)
                    userPreferences.saveUserLocation(data.user.location)
                    userPreferences.saveUserPhone(data.user.phone)
                    
                    Toast.makeText(this@LoginActivity, "Login Successful: ${data.user.full_name}", Toast.LENGTH_SHORT).show()
                    
                    // Navigate based on role
                    val role = data.user.role ?: "employee"
                    val targetActivity = if (role.equals("admin", ignoreCase = true)) {
                        AdminHomeActivity::class.java
                    } else {
                        EmployeeHomeActivity::class.java
                    }
                    val intent = Intent(this@LoginActivity, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    showErrorDialog("Login Error", "Invalid response from server")
                }
            } else {
                val message = result.exceptionOrNull()?.message ?: "Login failed"
                showErrorDialog("Login Error", message)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        signInButton.isEnabled = !isLoading
        emailInput.isEnabled = !isLoading
        passwordInput.isEnabled = !isLoading
        signInButton.text = if (isLoading) "Signing in..." else getString(R.string.sign_in)
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible
        passwordInput.setSelection(passwordInput.text.length)
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
