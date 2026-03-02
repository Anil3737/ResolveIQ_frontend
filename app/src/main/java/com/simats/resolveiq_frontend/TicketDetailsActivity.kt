package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.TeamMember
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ActivityTicketDetailsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import com.simats.resolveiq_frontend.utils.convertUtcToLocal
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.graphics.Color
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

        val initialTicket = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("ticket", Ticket::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("ticket") as? Ticket
        }
        if (initialTicket == null) {
            finish()
            return
        }

        setupUI(initialTicket)
        refreshTicketDetails(initialTicket.id)
    }

    private fun refreshTicketDetails(ticketId: Int) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = repository.getTicketDetails(ticketId)
            binding.progressBar.visibility = View.GONE
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.data != null) {
                    val ticket = response.data
                    setupUI(ticket)
                    setupWorkflowActions(ticket)
                }
            }
        }
    }

    private fun setupUI(ticket: Ticket) {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvTicketId.text = ticket.ticket_number ?: "IQ-IT-2026-${String.format("%06d", ticket.id)}"
        binding.tvTicketTitle.text = ticket.title
        binding.tvCreatedAt.text = "Created on: ${com.simats.resolveiq_frontend.utils.convertUtcToLocal(ticket.created_at)}"
        
        val locationRegex = "Location: (.*?)\\n".toRegex()
        val matchResult = ticket.description?.let { locationRegex.find(it) }
        val extractedLocation = matchResult?.groupValues?.get(1) ?: "N/A"
        
        val cleanDescription = if (matchResult != null) {
            ticket.description.replaceFirst("Location: .*?\\n\\n".toRegex(), "")
        } else {
            ticket.description ?: ""
        }

        binding.tvLocation.text = extractedLocation
        binding.tvDescription.text = cleanDescription
        
        if (ticket.sla_hours != null) {
            binding.tvSla.text = "${ticket.sla_hours} Hours"
        } else {
            binding.tvSla.text = when(ticket.priority.lowercase()) {
                "p1" -> "4 Hours"
                "p2" -> "8 Hours"
                "p3" -> "24 Hours"
                "p4" -> "48 Hours"
                else -> "TBD"
            }
        }

        // --- NEW: Risk Visibility ---
        val role = userPreferences.getUserRole()?.uppercase()
        
        // Format AI Explanation if available
        val explanationText = ticket.ai_explanation?.let {
            val parts = mutableListOf<String>()
            it["severity"]?.let { v -> parts.add("Severity: $v") }
            it["impact"]?.let { v -> parts.add("Impact: $v") }
            it["urgency"]?.let { v -> parts.add("Urgency: $v") }
            it["history"]?.let { v -> parts.add("History: $v") }
            it["complexity"]?.let { v -> parts.add("Complexity: $v") }
            if (parts.isNotEmpty()) "Risk Factors Breakdown:\n• ${parts.joinToString("\n• ")}" else null
        }

        // Style "RISK" in red
        val riskLabelStr = "AI RISK ASSESSMENT"
        val spannable = SpannableString(riskLabelStr)
        val start = riskLabelStr.indexOf("RISK")
        if (start != -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                start + "RISK".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.tvRiskLabel.text = spannable

        // ADMIN Visibility
        if (role == "ADMIN") {
            binding.tvAdminAiExplanationLabel.visibility = View.VISIBLE
            binding.tvAdminAiExplanation.visibility = View.VISIBLE
            binding.tvAdminAiExplanation.text = explanationText ?: "No AI analysis available for this ticket."
            
            // Still show risk assessment for admins for completeness
            binding.tvRiskLabel.visibility = View.VISIBLE
            binding.tvRiskScore.visibility = View.VISIBLE
            binding.tvRiskScore.text = "Risk: ${ticket.breach_risk ?: 0}%"
            binding.tvLeadAiExplanation.visibility = View.GONE
        } 
        // TEAM_LEAD / AGENT Visibility
        else if ((role == "TEAM_LEAD" || role == "AGENT") && ticket.breach_risk != null) {
            binding.tvAdminAiExplanationLabel.visibility = View.GONE
            binding.tvAdminAiExplanation.visibility = View.GONE
            
            binding.tvRiskLabel.visibility = View.VISIBLE
            binding.tvRiskScore.visibility = View.VISIBLE
            binding.tvRiskScore.text = "Risk: ${ticket.breach_risk}%"
            
            binding.tvLeadAiExplanation.visibility = View.VISIBLE
            binding.tvLeadAiExplanation.text = explanationText ?: "No detailed breakdown available."
        } else {
            binding.tvAdminAiExplanationLabel.visibility = View.GONE
            binding.tvAdminAiExplanation.visibility = View.GONE
            binding.tvRiskLabel.visibility = View.GONE
            binding.tvRiskScore.visibility = View.GONE
            binding.tvLeadAiExplanation.visibility = View.GONE
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
        
        // Reset visibilities
        binding.btnApprove.visibility = View.GONE
        binding.btnAccept.visibility = View.GONE
        binding.agentActiveActions.visibility = View.GONE

        when (role) {
            "TEAM_LEAD" -> {
                if (ticket.status.uppercase() == "OPEN") {
                    binding.btnApprove.visibility = View.VISIBLE
                    binding.btnApprove.setOnClickListener {
                        performAction(ticket.id) { repository.approveTicket(this@TicketDetailsActivity, ticket.id) }
                    }
                } else if (ticket.status.uppercase() == "APPROVED" && ticket.assigned_to == null) {
                    binding.btnAccept.visibility = View.VISIBLE
                    binding.btnAccept.text = "Assign to Agent"
                    binding.btnAccept.setOnClickListener {
                        showAssignAgentDialog(ticket)
                    }
                } else {
                    binding.actionLayout.visibility = View.GONE
                }
            }
            "AGENT" -> {
                if (ticket.can_accept == true) {
                    binding.btnAccept.visibility = View.VISIBLE
                    binding.btnAccept.text = "Accept"
                    binding.btnAccept.setOnClickListener {
                        performAction(ticket.id) { repository.updateTicketAction(this@TicketDetailsActivity, ticket.id, "ACCEPT") }
                    }
                } else if (ticket.can_resolve == true) {
                    binding.agentActiveActions.visibility = View.VISIBLE
                    binding.btnDecline.visibility = View.VISIBLE
                    binding.btnResolve.visibility = View.VISIBLE
                    binding.btnDecline.setOnClickListener {
                        performAction(ticket.id) { repository.updateTicketAction(this@TicketDetailsActivity, ticket.id, "DECLINE") }
                    }
                    binding.btnResolve.setOnClickListener {
                        performAction(ticket.id) { repository.updateTicketAction(this@TicketDetailsActivity, ticket.id, "RESOLVE") }
                    }
                } else if (ticket.can_decline == true) {
                    binding.agentActiveActions.visibility = View.VISIBLE
                    binding.btnDecline.visibility = View.VISIBLE
                    binding.btnResolve.visibility = View.GONE
                    binding.btnDecline.setOnClickListener {
                        performAction(ticket.id) { repository.updateTicketAction(this@TicketDetailsActivity, ticket.id, "DECLINE") }
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

    private fun showAssignAgentDialog(ticket: Ticket) {
        lifecycleScope.launch {
            val result = repository.getTeamMembers(this@TicketDetailsActivity)
            if (result.isSuccess) {
                val members = result.getOrNull() ?: emptyList()
                if (members.isEmpty()) {
                    Toast.makeText(this@TicketDetailsActivity, "No team members available", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val names = members.map { "${it.full_name ?: "Unknown"} (Active: ${it.active_tickets})" }.toTypedArray()
                AlertDialog.Builder(this@TicketDetailsActivity)
                    .setTitle("Assign Ticket to Agent")
                    .setItems(names) { _, which ->
                        val selectedAgent = members[which]
                        performAction(ticket.id) { repository.assignTicket(this@TicketDetailsActivity, ticket.id, selectedAgent.id) }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this@TicketDetailsActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performAction(ticketId: Int, actionCall: suspend () -> Result<*>) {
        lifecycleScope.launch {
            binding.actionLayout.isEnabled = false
            val result = actionCall()
            if (result.isSuccess) {
                Toast.makeText(this@TicketDetailsActivity, "Action performed successfully", Toast.LENGTH_SHORT).show()
                refreshTicketDetails(ticketId)
            } else {
                Toast.makeText(this@TicketDetailsActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                binding.actionLayout.isEnabled = true
            }
        }
    }
}
