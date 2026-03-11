package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.CreateTicketRequest
import com.simats.resolveiq_frontend.databinding.ActivityTicketWaitingBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TicketWaitingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketWaitingBinding
    private lateinit var ticketRepository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    companion object {
        private const val TAG = "TicketWaitingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketWaitingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
        userPreferences = UserPreferences(this)

        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val departmentId = intent.getIntExtra("department_id", -1)
        val issueType = intent.getStringExtra("issue_type") ?: ""
        val expectedResolutionTime = intent.getStringExtra("expected_resolution_time")
        val idempotencyKey = intent.getStringExtra("idempotency_key") ?: ""

        if (title.isEmpty() || departmentId == -1) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ── DUPLICATE CHECK ───────────────────────────────────────────
        // Before making the API call, check if a previous submission
        // with the same details was already completed successfully.
        if (userPreferences.isSubmissionCompleted() && userPreferences.isDuplicateSubmission(title, departmentId)) {
            Log.w(TAG, "Duplicate submission detected — ticket already created. Skipping API call.")
            Toast.makeText(this, "Ticket was already submitted successfully!", Toast.LENGTH_SHORT).show()
            // Navigate to success without re-submitting
            val intent = Intent(this@TicketWaitingActivity, TicketSuccessActivity::class.java).apply {
                putExtra("ticket_id", -1) // ID not available but ticket exists
            }
            startActivity(intent)
            finish()
            return
        }

        // ── SAVE PENDING SUBMISSION ───────────────────────────────────
        // Track this submission so we can detect duplicates on retry
        userPreferences.savePendingSubmission(idempotencyKey, title, departmentId)

        submitTicket(title, description, departmentId, issueType, expectedResolutionTime, idempotencyKey)
    }

    private fun submitTicket(
        title: String,
        description: String,
        departmentId: Int,
        issueType: String,
        expectedResolutionTime: String?,
        idempotencyKey: String
    ) {
        lifecycleScope.launch {
            // Brief delay so user sees the "Waiting" page
            delay(800)

            val request = CreateTicketRequest(
                title = title,
                description = description,
                departmentId = departmentId,
                issueType = issueType,
                expectedResolutionTime = expectedResolutionTime,
                idempotencyKey = idempotencyKey
            )

            val result = ticketRepository.createTicket(request)

            if (result.isSuccess) {
                // ── SUCCESS: Mark submission as completed ──────────────
                userPreferences.markSubmissionComplete()
                Log.i(TAG, "Ticket created successfully with idempotency key: $idempotencyKey")

                val ticketId = result.getOrNull() ?: -1
                val intent = Intent(this@TicketWaitingActivity, TicketSuccessActivity::class.java).apply {
                    putExtra("ticket_id", ticketId)
                }
                startActivity(intent)
                finish()
            } else {
                // ── FAILURE: Pass all data to TicketFailureActivity ───
                // so "Try Again" can retry with the SAME idempotency key
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e(TAG, "Ticket creation failed: $errorMsg (idempotency key: $idempotencyKey)")

                val intent = Intent(this@TicketWaitingActivity, TicketFailureActivity::class.java).apply {
                    putExtra("error_message", errorMsg)
                    // Pass all ticket data for retry
                    putExtra("title", title)
                    putExtra("description", description)
                    putExtra("department_id", departmentId)
                    putExtra("issue_type", issueType)
                    putExtra("expected_resolution_time", expectedResolutionTime)
                    putExtra("idempotency_key", idempotencyKey)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        // Prevent back during submission to avoid duplicate tickets or inconsistent state
        // Super call removed to disable back button
        Toast.makeText(this, "Submitting ticket, please wait...", Toast.LENGTH_SHORT).show()
    }
}
