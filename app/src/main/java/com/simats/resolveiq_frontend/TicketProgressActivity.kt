package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityTicketProgressBinding

class TicketProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ticketId = intent.getIntExtra("ticket_id", 0)
        val title = intent.getStringExtra("ticket_title") ?: "N/A"
        val description = intent.getStringExtra("ticket_description") ?: "N/A"
        val status = intent.getStringExtra("ticket_status") ?: "OPEN"

        setupUI(ticketId, title, description, status)
    }

    private fun setupUI(id: Int, title: String, description: String, status: String) {
        binding.tvTicketId.text = "#TK-${String.format("%04d", id)}"
        binding.tvTicketTitle.text = title
        binding.tvTicketDescription.text = description
        binding.tvStatusBadge.text = status.uppercase()

        // Handle back button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Apply same badge logic as details or adapter if needed
        when (status.lowercase()) {
            "open", "new" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                binding.tvStatusBadge.setTextColor(getColor(R.color.blue_600))
                binding.tvStatusBadge.backgroundTintList = getColorStateList(R.color.blue_50)
            }
            "in_progress", "assigned" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_in_progress)
                binding.tvStatusBadge.setTextColor(getColor(R.color.green_600))
                binding.tvStatusBadge.backgroundTintList = getColorStateList(R.color.green_50)
            }
            "pending" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_medium)
                binding.tvStatusBadge.setTextColor(getColor(R.color.orange_500))
                binding.tvStatusBadge.backgroundTintList = getColorStateList(R.color.orange_50)
            }
            "resolved", "closed" -> {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                binding.tvStatusBadge.setTextColor(getColor(R.color.gray_500))
                binding.tvStatusBadge.backgroundTintList = getColorStateList(R.color.gray_50)
            }
        }
    }
}
