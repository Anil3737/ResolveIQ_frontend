package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.api.AuthApiService
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.utils.TokenManager
import com.simats.resolveiq_frontend.utils.UserPreferences
import com.simats.resolveiq_frontend.viewmodel.LoginState
import com.simats.resolveiq_frontend.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePasswordVisibility: ImageView
    private lateinit var forgotPasswordText: TextView
    private lateinit var signInButton: Button
    private lateinit var emailSuccessText: TextView
    private lateinit var passwordSuccessText: TextView
    private lateinit var progressBar: ProgressBar

    private var isPasswordVisible = false
    
    // Dependencies
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize dependencies
        initializeDependencies()

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        signInButton = findViewById(R.id.signInButton)
        emailSuccessText = findViewById(R.id.emailSuccessText)
        passwordSuccessText = findViewById(R.id.passwordSuccessText)
        
        // Add progress bar programmatically if not in layout
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            isIndeterminate = true
        }
        // For simplicity, we'll use the button's enabled state to show loading

        // Set up password visibility toggle
        togglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }

        // Set up forgot password click
        forgotPasswordText.setOnClickListener {
            // TODO: Implement forgot password flow
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Set up sign in button click
        signInButton.setOnClickListener {
            performSignIn()
        }

        // Add text watchers to show success messages
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && emailInput.text.isNotEmpty()) {
                emailSuccessText.visibility = View.VISIBLE
            }
        }

        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && passwordInput.text.isNotEmpty()) {
                passwordSuccessText.visibility = View.VISIBLE
            }
        }
        
        // Observe login state
        observeLoginState()
        
        // Check if already logged in
        if (authRepository.isLoggedIn()) {
            navigateToMain()
        }
    }
    
    /**
     * Initialize dependencies (Repository, ViewModel)
     */
    private fun initializeDependencies() {
        val tokenManager = TokenManager.getInstance(this)
        val userPreferences = UserPreferences.getInstance(this)
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val authApiService = retrofit.create(AuthApiService::class.java)
        
        authRepository = AuthRepository(authApiService, tokenManager, userPreferences)
        loginViewModel = LoginViewModel(authRepository)
    }
    
    /**
     * Observe login state changes from ViewModel
     */
    private fun observeLoginState() {
        loginViewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Idle -> {
                    // Reset UI
                    setLoading(false)
                }
                
                is LoginState.Loading -> {
                    // Show loading
                    setLoading(true)
                }
                
                is LoginState.Success -> {
                    // Login successful, navigate to MainActivity
                    setLoading(false)
                    Toast.makeText(
                        this,
                        "Welcome, ${state.user.full_name}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                }
                
                is LoginState.Error -> {
                    // Show error message
                    setLoading(false)
                    Toast.makeText(
                        this,
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Set loading state UI
     */
    private fun setLoading(loading: Boolean) {
        signInButton.isEnabled = !loading
        emailInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
        
        if (loading) {
            signInButton.text = "Signing in..."
        } else {
            signInButton.text = getString(R.string.sign_in)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            isPasswordVisible = false
        } else {
            // Show password
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility)
            isPasswordVisible = true
        }
        // Move cursor to end of text
        passwordInput.setSelection(passwordInput.text.length)
    }

    private fun performSignIn() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Basic validation
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            emailInput.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Invalid email format"
            emailInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        // Clear previous errors
        emailInput.error = null
        passwordInput.error = null
        
        // Call ViewModel to perform login
        loginViewModel.login(email, password)
    }
    
    /**
     * Navigate to MainActivity
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
