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
            binding.tvTicketId.text = "TKT-${ticket.id}"
            
            // Status styling
            binding.tvTicketStatus.text = ticket.status.replace("_", " ")
            binding.tvTicketStatus.setTextColor(Color.parseColor(getStatusColor(ticket.status)))
            
            // Tags
            binding.tvTag1.text = ticket.priority
            binding.tvTag1.setTextColor(Color.parseColor(getPriorityColor(ticket.priority)))
            
            binding.root.setOnClickListener { onTicketClick(ticket) }
        }
    }

    private fun getStatusColor(status: String): String {
        return when (status.uppercase()) {
            "OPEN" -> "#2E7D32" // Green
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
