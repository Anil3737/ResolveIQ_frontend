package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.AuthApiService
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.PasswordResetRequest
import retrofit2.Response
import kotlinx.coroutines.launch

class PasswordResetRequestActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etEmployeeId: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var resultCard: View
    private lateinit var tvResultMessage: TextView
    private lateinit var llApprovedPassword: View
    private lateinit var tvTempPassword: TextView
    private lateinit var btnCopy: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset_request)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etEmployeeId = findViewById(R.id.etEmployeeId)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        resultCard = findViewById(R.id.resultCard)
        tvResultMessage = findViewById(R.id.tvResultMessage)
        llApprovedPassword = findViewById(R.id.llApprovedPassword)
        tvTempPassword = findViewById(R.id.tvTempPassword)
        btnCopy = findViewById(R.id.btnCopy)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            submitRequest()
        }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Temporary Password", tvTempPassword.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitRequest() {
        val email = etEmail.text.toString().trim()
        val empId = etEmployeeId.text.toString().trim()

        if (email.isEmpty() || empId.isEmpty()) {
            showError("Both fields are required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address")
            return
        }

        setLoading(true)
        tvError.visibility = View.GONE
        resultCard.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val authApi = RetrofitClient.getAuthApi(this@PasswordResetRequestActivity)
                val request = PasswordResetRequest(email, empId)
                
                // Step 1 — Check if there is an approved or pending request first
                val checkResponse = authApi.checkPasswordReset(request)
                
                if (checkResponse.isSuccessful) {
                    val body = checkResponse.body()
                    if (body != null) {
                        if (body.success && body.temp_password != null) {
                            // Case: APPROVED
                            showApprovedResult(body.temp_password)
                        } else if (!body.success && body.message?.contains("pending", ignoreCase = true) == true) {
                            // Case: PENDING
                            showPendingResult(body.message)
                        } else if (!body.success && body.message?.contains("declined", ignoreCase = true) == true) {
                            // Case: DECLINED
                            showError(body.message)
                        } else {
                            // Case: Not found or something else -> call request
                            performInitialRequest(authApi, request)
                        }
                    } else {
                        performInitialRequest(authApi, request)
                    }
                } else if (checkResponse.code() == 404) {
                    val errorBody = checkResponse.errorBody()?.string()
                    if (errorBody?.contains("Invalid Data") == true) {
                        showError("Invalid Data") // Display in red
                    } else {
                        // Probably no request, so create one
                        performInitialRequest(authApi, request)
                    }
                } else {
                    showError("Error checking request status. Please try again.")
                }
            } catch (e: Exception) {
                showError("An error occurred. Please check your connection.")
                e.printStackTrace()
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun performInitialRequest(authApi: AuthApiService, request: PasswordResetRequest) {
        try {
            val response = authApi.requestPasswordReset(request)
            if (response.success) {
                showPendingResult(response.message ?: "Request sent to administrator")
            } else {
                showError(response.message ?: "Failed to submit request")
            }
        } catch (e: Exception) {
            showError("Failed to send request. Please try again.")
            e.printStackTrace()
        }
    }

    private fun showApprovedResult(password: String) {
        tvResultMessage.text = "Your password reset has been APPROVED!"
        tvResultMessage.setTextColor(android.graphics.Color.parseColor("#15803D")) // Greenish
        tvTempPassword.text = password
        llApprovedPassword.visibility = View.VISIBLE
        resultCard.visibility = View.VISIBLE
        btnSubmit.visibility = View.GONE
    }

    private fun showPendingResult(message: String) {
        tvResultMessage.text = message
        tvResultMessage.setTextColor(android.graphics.Color.parseColor("#0369A1")) // Blueish
        llApprovedPassword.visibility = View.GONE
        resultCard.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !isLoading
        btnSubmit.text = if (isLoading) "" else "Send Reset Request"
        etEmail.isEnabled = !isLoading
        etEmployeeId.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
        // Ensure color is red for "Invalid Data" or other errors
        tvError.setTextColor(android.graphics.Color.RED)
    }
}
