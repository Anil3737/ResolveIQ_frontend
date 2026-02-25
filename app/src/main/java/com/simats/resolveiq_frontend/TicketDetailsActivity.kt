package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ActivityTicketDetailsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import com.simats.resolveiq_frontend.utils.convertUtcToLocal
import kotlinx.coroutines.launch

class TicketDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailsBinding
    private lateinit var repository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        repository = TicketRepository(RetrofitClient.getTicketApi(this))

        val ticket = intent.getSerializableExtra("ticket") as? Ticket
        if (ticket == null) {
            finish()
            return
        }

        setupUI(ticket)
        setupWorkflowActions(ticket)
    }

    private fun setupUI(ticket: Ticket) {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvTicketId.text = ticket.ticket_number ?: "IQ-IT-2026-${String.format("%06d", ticket.id)}"
        binding.tvTicketTitle.text = ticket.title
        binding.tvCreatedAt.text = "Created on: ${convertUtcToLocal(ticket.created_at)}"
        
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

        binding.btnViewProgress.setOnClickListener {
            val intent = Intent(this, TicketProgressActivity::class.java).apply {
                putExtra("ticket_id", ticket.id)
                putExtra("ticket_number", ticket.ticket_number)
                putExtra("ticket_title", ticket.title)
                putExtra("ticket_description", ticket.description)
                putExtra("ticket_status", ticket.status)
            }
            startActivity(intent)
        }
    }

    private fun setupWorkflowActions(ticket: Ticket) {
        val role = userPreferences.getUserRole()?.uppercase()
        binding.actionLayout.visibility = View.VISIBLE

        when (role) {
            "TEAM_LEAD" -> {
                if (ticket.status.uppercase() == "OPEN") {
                    binding.btnApprove.visibility = View.VISIBLE
                    binding.btnApprove.setOnClickListener {
                        performAction { repository.approveTicket(this, ticket.id) }
                    }
                } else if (ticket.status.uppercase() == "APPROVED" && ticket.assigned_to == null) {
                    // TL can also pick up approved tickets if they want, or we can just leave it for agents
                    // For now, let's just make sure they see the ticket.
                    binding.actionLayout.visibility = View.GONE
                } else {
                    binding.actionLayout.visibility = View.GONE
                }
            }
            "AGENT" -> {
                if (ticket.can_accept == true) {
                    binding.btnAccept.visibility = View.VISIBLE
                    binding.btnAccept.setOnClickListener {
                        performAction { repository.updateTicketAction(this, ticket.id, "ACCEPT") }
                    }
                } else if (ticket.can_decline == true || ticket.can_resolve == true) {
                    binding.agentActiveActions.visibility = View.VISIBLE
                    binding.btnDecline.setOnClickListener {
                        performAction { repository.updateTicketAction(this, ticket.id, "DECLINE") }
                    }
                    binding.btnResolve.setOnClickListener {
                        performAction { repository.updateTicketAction(this, ticket.id, "RESOLVE") }
                    }
                } else {
                    binding.actionLayout.visibility = View.GONE
                }
            }
            else -> {
                binding.actionLayout.visibility = View.GONE
            }
        }
    }

    private fun performAction(actionCall: suspend () -> Result<*>) {
        lifecycleScope.launch {
            binding.actionLayout.isEnabled = false
            val result = actionCall()
            if (result.isSuccess) {
                Toast.makeText(this@TicketDetailsActivity, "Action performed successfully", Toast.LENGTH_SHORT).show()
                finish() // Go back and refresh
            } else {
                Toast.makeText(this@TicketDetailsActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                binding.actionLayout.isEnabled = true
            }
        }
    }
}
