package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ActivityTicketDetailsBinding

class TicketDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ticket = intent.getSerializableExtra("ticket") as? Ticket
        if (ticket == null) {
            finish()
            return
        }

        setupUI(ticket)
    }

    private fun setupUI(ticket: Ticket) {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvTicketId.text = "#TIC-${String.format("%06d", ticket.id)}"
        binding.tvTicketTitle.text = ticket.title
        binding.tvCreatedAt.text = "Created on: ${ticket.created_at ?: "N/A"}"
        
        // Extract location from description if it follows our pattern "Location: ... \n\n ..."
        val locationRegex = "Location: (.*?)\\n".toRegex()
        val matchResult = locationRegex.find(ticket.description)
        val extractedLocation = matchResult?.groupValues?.get(1) ?: "N/A"
        
        // Clean description (remove location prefix for display if it matches)
        val cleanDescription = if (matchResult != null) {
            ticket.description.replaceFirst("Location: .*?\\n\\n".toRegex(), "")
        } else {
            ticket.description
        }

        binding.tvLocation.text = extractedLocation
        binding.tvDescription.text = cleanDescription
        
        // SLA placeholder (Priority based mockup)
        binding.tvSla.text = when(ticket.priority.lowercase()) {
            "high" -> "4 Hours"
            "medium" -> "8 Hours"
            "low" -> "24 Hours"
            else -> "TBD"
        }
    }
}
