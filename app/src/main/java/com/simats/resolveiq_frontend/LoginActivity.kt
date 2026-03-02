package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvLoginError: TextView
    private lateinit var ivTogglePasswordVisibility: ImageView
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

    private fun initializeDependencies() {
        val authApi = RetrofitClient.getAuthApi(this)
        authRepository = AuthRepository(authApi)
        userPreferences = UserPreferences(this)
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        tvEmailError = findViewById(R.id.tvEmailError)
        tvPasswordError = findViewById(R.id.tvPasswordError)
        tvLoginError = findViewById(R.id.tvLoginError)
        ivTogglePasswordVisibility = findViewById(R.id.togglePasswordVisibility)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        signInButton = findViewById(R.id.signInButton)

        emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvEmailError.text = ""
                tvLoginError.visibility = android.view.View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvPasswordError.text = ""
                tvLoginError.visibility = android.view.View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<Button>(R.id.btnCreateAccount).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        signInButton.setOnClickListener {
            performLogin()
        }

        ivTogglePasswordVisibility.setOnClickListener {
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
            if (email.isEmpty()) {
                tvEmailError.text = "Email cannot be empty"
            }
            if (password.isEmpty()) {
                tvPasswordError.text = "Password cannot be empty"
            }
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
                    userPreferences.saveToken(data.access_token)
                    userPreferences.saveUserId(data.user.id)
                    userPreferences.saveUserRole(data.user.role ?: "employee")
                    userPreferences.saveUserName(data.user.full_name ?: "User")
                    userPreferences.saveUserEmail(data.user.email ?: "")
                    userPreferences.saveUserLocation(data.user.location)
                    userPreferences.saveUserPhone(data.user.phone)

                    Toast.makeText(
                        this@LoginActivity,
                        "Login Successful: ${data.user.full_name}",
                        Toast.LENGTH_SHORT
                    ).show()

                    val role = (data.user.role ?: "EMPLOYEE").uppercase()

                    if (data.user.requirePasswordChange) {
                        val intent = Intent(this@LoginActivity, FirstLoginPromptActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val targetActivity = when (role) {
                            "ADMIN" -> AdminHomeActivity::class.java
                            "TEAM_LEAD" -> TeamLeadHomeActivity::class.java
                            "AGENT" -> SupportAgentHomeActivity::class.java
                            else -> EmployeeHomeActivity::class.java
                        }
                        val intent = Intent(this@LoginActivity, targetActivity)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    finish()
                }
            } else {
                val exception = result.exceptionOrNull()
                val message = exception?.message ?: "Login failed"

                // Clear previous errors
                tvEmailError.text = ""
                tvPasswordError.text = ""
                tvLoginError.visibility = android.view.View.GONE

                // Show a single combined error for invalid credentials (401)
                val is401 = (exception is retrofit2.HttpException && exception.code() == 401) ||
                        message.contains("401") ||
                        message.contains("Unauthorized", ignoreCase = true)

                if (is401) {
                    tvLoginError.text = "The email address or password you entered is invalid"
                    tvLoginError.visibility = android.view.View.VISIBLE
                } else {
                    Log.e("LoginActivity", "Login error: $message")
                }
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
            ivTogglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible
        passwordInput.setSelection(passwordInput.text.length)
    }
}
