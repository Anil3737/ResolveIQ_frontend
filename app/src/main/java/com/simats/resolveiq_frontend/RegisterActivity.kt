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
import android.text.Editable
import android.text.TextWatcher
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
        setupTermsCheckbox()
    }

    private fun setupTermsCheckbox() {
        val fullText = getString(R.string.terms_agreement)
        val spannableString = SpannableString(fullText)

        val termsText = "Terms of Service"
        val privacyText = "Privacy Policy"

        val termsStart = fullText.indexOf(termsText)
        val termsEnd = termsStart + termsText.length

        val privacyStart = fullText.indexOf(privacyText)
        val privacyEnd = privacyStart + privacyText.length

        // Color for links
        val linkColor = Color.parseColor("#1E3A8A")

        // Terms of Service ClickableSpan
        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, AgreementsActivity::class.java))
            }
        }

        // Privacy Policy ClickableSpan
        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, AgreementsActivity::class.java))
            }
        }

        if (termsStart != -1) {
            spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(linkColor), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (privacyStart != -1) {
            spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(linkColor), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.cbTerms.text = spannableString
        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupListeners() {
        // Sign In Link
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Clear duplicate-error when user edits Employee ID or Company Email
        val clearRegisterError = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvRegisterError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etEmployeeId.addTextChangedListener(clearRegisterError)
        binding.etCompanyEmail.addTextChangedListener(clearRegisterError)
        
        // Real-time Employee ID uniqueness check
        var checkJob: kotlinx.coroutines.Job? = null
        binding.etEmployeeId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkJob?.cancel()
                val empId = s.toString().trim().uppercase()
                if (empId.length >= 3) {
                    checkJob = lifecycleScope.launch {
                        delay(500) // Debounce
                        val result = authRepository.checkEmployeeIdExists(empId)
                        if (result.isSuccess && result.getOrNull() == true) {
                            binding.tvRegisterError.text = "Employee Id Already exist"
                            binding.tvRegisterError.visibility = View.VISIBLE
                        } else {
                            if (binding.tvRegisterError.text == "Employee Id Already exist") {
                                binding.tvRegisterError.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Live email domain validation
        binding.etCompanyEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && !isValidEmail(email)) {
                    binding.tvEmailError.text = "Please enter a valid email address"
                    binding.tvEmailError.visibility = View.VISIBLE
                } else {
                    binding.tvEmailError.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Live password strength validation
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pwd = s.toString()
                if (pwd.isNotEmpty()) {
                    val err = passwordStrengthError(pwd)
                    if (err != null) {
                        binding.tvPasswordError.text = err
                        binding.tvPasswordError.visibility = View.VISIBLE
                    } else {
                        binding.tvPasswordError.visibility = View.GONE
                    }
                } else {
                    binding.tvPasswordError.visibility = View.GONE
                }
                // also re-check confirm match if confirm has text
                val confirm = binding.etConfirmPassword.text.toString()
                if (confirm.isNotEmpty()) {
                    if (s.toString() != confirm) {
                        binding.tvConfirmPasswordError.text = "Passwords do not match"
                        binding.tvConfirmPasswordError.visibility = View.VISIBLE
                    } else {
                        binding.tvConfirmPasswordError.visibility = View.GONE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Live confirm-password match validation
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val confirm = s.toString()
                val pwd = binding.etPassword.text.toString()
                if (confirm.isNotEmpty()) {
                    if (confirm != pwd) {
                        binding.tvConfirmPasswordError.text = "Passwords do not match"
                        binding.tvConfirmPasswordError.visibility = View.VISIBLE
                    } else {
                        binding.tvConfirmPasswordError.visibility = View.GONE
                    }
                } else {
                    binding.tvConfirmPasswordError.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
        val email = binding.etCompanyEmail.text
            .toString()
            .trim()
            .replace(" ", "")   // removes accidental spaces
        val phone = binding.etEmployeeId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val location = binding.etOfficeLocation.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Email domain check
        if (!isValidEmail(email)) {
            binding.tvEmailError.text = "Please enter a valid email address"
            binding.tvEmailError.visibility = View.VISIBLE
            return
        }

        // Password strength check
        val pwdError = passwordStrengthError(password)
        if (pwdError != null) {
            binding.tvPasswordError.text = pwdError
            binding.tvPasswordError.visibility = View.VISIBLE
            return
        }

        // Confirm password match check
        if (password != confirmPassword) {
            binding.tvConfirmPasswordError.text = "Passwords do not match"
            binding.tvConfirmPasswordError.visibility = View.VISIBLE
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
                department_id = 1, // Default to 1
                location = if (location.isNotEmpty()) location else null
            )
            
            val result = authRepository.register(request)
            
            setLoading(false)
            
            if (result.isSuccess) {
                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            } else {
                val e = result.exceptionOrNull()
                val message = e?.message ?: ""

                // Detect 400 / conflict — Employee ID or Email already exists
                val is400 = (e is retrofit2.HttpException && e.code() == 400) ||
                        message.contains("400") ||
                        message.contains("already", ignoreCase = true) ||
                        message.contains("exist", ignoreCase = true) ||
                        message.contains("conflict", ignoreCase = true) ||
                        message.contains("duplicate", ignoreCase = true)

                if (is400) {
                    binding.tvRegisterError.text = "The Employee ID and Email Address Already Exist"
                    binding.tvRegisterError.visibility = View.VISIBLE
                } else {
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
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun passwordStrengthError(password: String): String? {
        if (password.length <= 8) return "Password must be more than 8 characters"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least one uppercase letter"
        if (!password.any { it.isLowerCase() }) return "Password must contain at least one lowercase letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        if (!password.any { !it.isLetterOrDigit() }) return "Password must contain at least one special character"
        return null
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnCreateAccountSubmit.isEnabled = !isLoading
        binding.btnCreateAccountSubmit.text = if (isLoading) "" else getString(R.string.create_account)
        binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }
}
