package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.CreateTicketRequest
import com.simats.resolveiq_frontend.databinding.ActivityTicketWaitingBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TicketWaitingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketWaitingBinding
    private lateinit var ticketRepository: TicketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketWaitingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)

        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val departmentId = intent.getIntExtra("department_id", -1)
        val issueType = intent.getStringExtra("issue_type") ?: ""
        val expectedResolutionTime = intent.getStringExtra("expected_resolution_time")

        if (title.isEmpty() || departmentId == -1) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        submitTicket(title, description, departmentId, issueType, expectedResolutionTime)
    }

    private fun submitTicket(
        title: String,
        description: String,
        departmentId: Int,
        issueType: String,
        expectedResolutionTime: String?
    ) {
        lifecycleScope.launch {
            // Give the user a moment to see the "Waiting" page even if the API is fast
            delay(1500)

            val request = CreateTicketRequest(
                title = title,
                description = description,
                departmentId = departmentId,
                issueType = issueType,
                expectedResolutionTime = expectedResolutionTime
            )

            val result = ticketRepository.createTicket(request)

            if (result.isSuccess) {
                val ticketId = result.getOrNull() ?: -1
                val intent = Intent(this@TicketWaitingActivity, TicketSuccessActivity::class.java).apply {
                    putExtra("ticket_id", ticketId)
                }
                startActivity(intent)
                finish()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                val intent = Intent(this@TicketWaitingActivity, TicketFailureActivity::class.java).apply {
                    putExtra("error_message", errorMsg)
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
