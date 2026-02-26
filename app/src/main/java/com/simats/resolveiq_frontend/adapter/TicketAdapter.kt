package com.simats.resolveiq_frontend.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ItemTicketBinding

class TicketAdapter(
    private var tickets: List<Ticket>,
    private val onTicketClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(private val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ticket: Ticket) {
            binding.tvTicketTitle.text = ticket.title
            binding.tvTicketDescription.text = ticket.description
            binding.tvTicketId.text = ticket.ticket_number ?: "IQ-IT-2026-${String.format("%06d", ticket.id)}"

            // Status styling
            binding.tvTicketStatus.text = ticket.status.replace("_", " ")
            binding.tvTicketStatus.setTextColor(Color.parseColor(getStatusColor(ticket.status)))

            // Priority tag
            binding.tvTag1.text = ticket.priority
            binding.tvTag1.setTextColor(Color.parseColor(getPriorityColor(ticket.priority)))

            // Department as second tag
            if (!ticket.department_name.isNullOrBlank()) {
                binding.tvTag2.text = ticket.department_name
                binding.tvTag2.visibility = android.view.View.VISIBLE
            } else {
                binding.tvTag2.visibility = android.view.View.GONE
            }

            // Assignee name
            binding.tvAssigneeName.text = ticket.assigned_to_name ?: "Unassigned"

            // SLA time remaining
            val secondsLeft = ticket.sla_remaining_seconds
            if (secondsLeft != null && secondsLeft > 0) {
                val hours = secondsLeft / 3600
                val mins = (secondsLeft % 3600) / 60
                binding.tvTimeRemaining.text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                binding.tvTimeRemaining.setTextColor(Color.parseColor(if (secondsLeft < 3600) "#EF4444" else "#F57C00"))
            } else if (ticket.sla_breached == true) {
                binding.tvTimeRemaining.text = "SLA Breached"
                binding.tvTimeRemaining.setTextColor(Color.parseColor("#EF4444"))
            } else {
                binding.tvTimeRemaining.text = ""
            }

            binding.root.setOnClickListener { onTicketClick(ticket) }
        }
    }

    private fun getStatusColor(status: String): String {
        return when (status.uppercase()) {
            "OPEN" -> "#2E7D32" // Green
            "APPROVED" -> "#7B1FA2" // Purple
            "IN_PROGRESS" -> "#F57C00" // Orange
            "RESOLVED" -> "#1976D2" // Blue
            "CLOSED" -> "#757575" // Grey
            else -> "#000000"
        }
    }

    private fun getPriorityColor(priority: String): String {
        return when (priority.uppercase()) {
            "P1", "HIGH" -> "#D32F2F" // Red
            "P2", "MEDIUM" -> "#F57C00" // Orange
            "P3", "LOW" -> "#388E3C" // Green
            else -> "#000000"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    override fun getItemCount() = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
}
