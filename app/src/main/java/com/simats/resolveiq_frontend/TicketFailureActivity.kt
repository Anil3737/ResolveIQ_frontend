package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityTicketFailureBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class TicketFailureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketFailureBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var ticketRepository: TicketRepository

    // Flag to prevent double-tap on "Try Again" button
    private var isRetrying = false

    companion object {
        private const val TAG = "TicketFailureActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketFailureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userPreferences = UserPreferences(this)
        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
        
        val errorMessage = intent.getStringExtra("error_message")
        if (!errorMessage.isNullOrEmpty()) {
            binding.tvErrorMessage.text = errorMessage
        }

        // Retrieve all ticket data passed from TicketWaitingActivity for retry
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val departmentId = intent.getIntExtra("department_id", -1)
        val issueType = intent.getStringExtra("issue_type") ?: ""
        val expectedResolutionTime = intent.getStringExtra("expected_resolution_time")
        val idempotencyKey = intent.getStringExtra("idempotency_key") ?: ""

        binding.btnTryAgain.setOnClickListener {
            if (isRetrying) return@setOnClickListener

            // ── SMART RETRY ──────────────────────────────────────────
            // First check if the ticket was actually created despite the
            // timeout/error. If it was, skip re-submission entirely.
            if (title.isNotEmpty() && departmentId != -1) {
                isRetrying = true
                binding.btnTryAgain.isEnabled = false
                binding.btnTryAgain.alpha = 0.5f
                Toast.makeText(this, "Checking ticket status...", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    // Check if the submission was already marked as completed
                    if (userPreferences.isSubmissionCompleted() && userPreferences.isDuplicateSubmission(title, departmentId)) {
                        Log.i(TAG, "Ticket was already created despite the error. Navigating to success.")
                        userPreferences.clearPendingSubmission()
                        val intent = Intent(this@TicketFailureActivity, TicketSuccessActivity::class.java).apply {
                            putExtra("ticket_id", -1)
                        }
                        startActivity(intent)
                        finish()
                        return@launch
                    }

                    // Check with server if the ticket already exists by fetching recent tickets
                    try {
                        val ticketsResult = ticketRepository.getTickets(limit = 10)
                        if (ticketsResult.isSuccess) {
                            val recentTickets = ticketsResult.getOrNull() ?: emptyList()
                            // Look for a ticket matching the title we tried to create
                            val existingTicket = recentTickets.find { it.title == title }
                            if (existingTicket != null) {
                                Log.i(TAG, "Found existing ticket with matching title: ${existingTicket.id}. Skipping re-submission.")
                                userPreferences.markSubmissionComplete()
                                val intent = Intent(this@TicketFailureActivity, TicketSuccessActivity::class.java).apply {
                                    putExtra("ticket_id", existingTicket.id)
                                }
                                startActivity(intent)
                                finish()
                                return@launch
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not check for existing tickets: ${e.message}")
                        // Continue with retry if check fails
                    }

                    // ── NO DUPLICATE FOUND — Safe to retry ────────────
                    // Navigate directly to TicketWaitingActivity with the
                    // SAME idempotency key (skipping CreateTicketActivity)
                    Log.i(TAG, "No duplicate found. Retrying submission with same idempotency key: $idempotencyKey")
                    val retryIntent = Intent(this@TicketFailureActivity, TicketWaitingActivity::class.java).apply {
                        putExtra("title", title)
                        putExtra("description", description)
                        putExtra("department_id", departmentId)
                        putExtra("issue_type", issueType)
                        putExtra("expected_resolution_time", expectedResolutionTime)
                        putExtra("idempotency_key", idempotencyKey)
                    }
                    startActivity(retryIntent)
                    finish()
                }
            } else {
                // Fallback: no ticket data available, go back to create screen
                val intent = Intent(this, CreateTicketActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        }

        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val role = userPreferences.getUserRole() ?: "employee"
                    val targetActivity = if (role.equals("admin", ignoreCase = true)) {
                        AdminHomeActivity::class.java
                    } else {
                        EmployeeHomeActivity::class.java
                    }
                    val intent = Intent(this, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_tickets -> {
                    val intent = Intent(this, MyTicketsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = 0 // Unselect
        // Re-enable retry button when returning to this screen
        isRetrying = false
        binding.btnTryAgain.isEnabled = true
        binding.btnTryAgain.alpha = 1.0f
    }
}
