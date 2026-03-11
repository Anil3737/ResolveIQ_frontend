package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityCreateTicketBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import java.util.UUID

class CreateTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTicketBinding
    private lateinit var ticketRepository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    // Flag to prevent double-tap / rapid multiple submissions
    private var isSubmitting = false
    
    // Hardcoded issue types for demo
    private val issueTypes = listOf("Network Issue", "Hardware Failure", "Software Installation", "Application Downtime / Application Issues", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupUI()
    }

    private fun setupDependencies() {
        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
        userPreferences = UserPreferences(this)
    }

    private fun setupUI() {
        // Back Button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Issue Type Dropdown
        binding.layoutIssueType.setOnClickListener {
            showIssueTypeDialog()
        }

        // Submit Button — guarded against double-tap
        binding.btnSubmit.setOnClickListener {
            if (!isSubmitting) {
                submitTicket()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-enable submit when user returns to this screen (e.g., after navigating back)
        isSubmitting = false
        binding.btnSubmit.isEnabled = true
        binding.btnSubmit.alpha = 1.0f
    }
    
    private fun showIssueTypeDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Issue Type")
        builder.setItems(issueTypes.toTypedArray()) { _, which ->
            binding.tvIssueType.text = issueTypes[which]
            binding.tvIssueType.setTextColor(resources.getColor(R.color.black, theme))
        }
        builder.show()
    }

    private fun submitTicket() {
        val issueType = binding.tvIssueType.text.toString()
        val location = binding.etLocation.text.toString()
        val description = binding.etDescription.text.toString()
        // Resolution time ignored as per strict backend contract
        
        if (issueType == "Choose an issue type..." || issueType.isBlank()) {
            Toast.makeText(this, "Please select an issue type", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (location.isBlank()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (description.isBlank()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Combine Location into Title and Description
        val fullTitle = "[$issueType] at $location"
        val fullDescription = "Location: $location\n\n$description"
        val expectedResolutionTime = binding.etResolutionTime.text.toString()
        
        // Map issue type → department_id (must match DEPARTMENT_RANGES in ticket_id_generator.py)
        val departmentId = when (issueType) {
            "Network Issue"                              -> 1
            "Hardware Failure"                           -> 2
            "Software Installation"                      -> 3
            "Application Downtime / Application Issues"  -> 4
            "Other"                                      -> 5
            else -> {
                Toast.makeText(this, "Invalid department selected. Please try again.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // ── DUPLICATE GUARD ──────────────────────────────────────────
        // Check if a submission with the same title+department was made
        // within the last 2 minutes (prevents re-submit after timeout)
        if (userPreferences.isDuplicateSubmission(fullTitle, departmentId)) {
            Toast.makeText(
                this,
                "A ticket with the same details was recently submitted. Please wait before trying again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // ── LOCK SUBMIT BUTTON ───────────────────────────────────────
        // Prevent double-tap by disabling immediately
        isSubmitting = true
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.alpha = 0.5f

        // ── GENERATE IDEMPOTENCY KEY ─────────────────────────────────
        // Unique UUID for this submission attempt
        val idempotencyKey = UUID.randomUUID().toString()
        
        // Navigate to Waiting Activity with all data + idempotency key
        val intent = Intent(this, TicketWaitingActivity::class.java).apply {
            putExtra("title", fullTitle)
            putExtra("description", fullDescription)
            putExtra("department_id", departmentId)
            putExtra("issue_type", issueType)
            putExtra("expected_resolution_time", expectedResolutionTime)
            putExtra("idempotency_key", idempotencyKey)
        }
        startActivity(intent)
        finish()
    }
}
