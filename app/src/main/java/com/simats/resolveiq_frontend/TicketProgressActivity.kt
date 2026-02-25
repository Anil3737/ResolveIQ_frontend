package com.simats.resolveiq_frontend

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.ProgressStep
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.data.model.TicketProgress
import com.simats.resolveiq_frontend.databinding.ActivityTicketProgressBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.launch

class TicketProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketProgressBinding
    private lateinit var repository: TicketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TicketRepository(RetrofitClient.getTicketApi(this))

        val ticketId = intent.getIntExtra("ticket_id", -1)
        if (ticketId == -1) {
            finish()
            return
        }

        fetchTicketProgress(ticketId)

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchTicketProgress(ticketId: Int) {
        lifecycleScope.launch {
            val result = repository.getTicketDetails(ticketId)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    updateUI(data.data, data.progress)
                }
            } else {
                Toast.makeText(this@TicketProgressActivity, "Failed to load progress", Toast.LENGTH_SHORT).show()
                // Fallback to basic info if available
                val ticketNum = intent.getStringExtra("ticket_number") ?: "IQ-IT-2026-${String.format("%06d", ticketId)}"
                val title = intent.getStringExtra("ticket_title") ?: "N/A"
                val description = intent.getStringExtra("ticket_description") ?: "N/A"
                val status = intent.getStringExtra("ticket_status") ?: "OPEN"
                setupBasicInfo(ticketNum, title, description, status)
            }
        }
    }

    private fun updateUI(ticket: Ticket, progress: TicketProgress) {
        setupBasicInfo(
            ticket.ticket_number ?: "IQ-IT-2026-${String.format("%06d", ticket.id)}",
            ticket.title,
            ticket.description,
            ticket.status
        )

        updateStep(binding.dot1, binding.line1, progress.created)
        updateStep(binding.dot2, binding.line2, progress.approved)
        updateStep(binding.dot3, binding.line3, progress.assigned)
        updateStep(binding.dot4, binding.line4, progress.accepted)
        updateStep(binding.dot5, binding.line5, progress.resolved)
        updateStep(binding.dot6, null, progress.closed)
    }

    private fun setupBasicInfo(ticketNumber: String, title: String, description: String, status: String) {
        binding.tvTicketId.text = ticketNumber
        binding.tvTicketTitle.text = title
        binding.tvTicketDescription.text = description
        binding.tvStatusBadge.text = status.uppercase()

        when (status.lowercase()) {
            "open", "new" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                binding.tvStatusBadge.setTextColor(getColor(R.color.blue_600))
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(getColor(R.color.blue_50))
            }
            "in_progress", "assigned" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_in_progress)
                binding.tvStatusBadge.setTextColor(getColor(R.color.green_600))
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green_50))
            }
            "resolved", "closed" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                binding.tvStatusBadge.setTextColor(getColor(R.color.gray_500))
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(getColor(R.color.gray_50))
            }
        }
    }

    private fun updateStep(dot: View, line: View?, step: ProgressStep) {
        if (step.status) {
            dot.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green_500))
            line?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green_500))
        } else {
            dot.backgroundTintList = ColorStateList.valueOf(getColor(R.color.gray_300))
            line?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.gray_200))
        }
    }
}
