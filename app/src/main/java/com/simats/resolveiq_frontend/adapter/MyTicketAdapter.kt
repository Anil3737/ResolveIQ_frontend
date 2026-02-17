package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ItemMyTicketBinding

class MyTicketAdapter(
    private var tickets: List<Ticket>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<MyTicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(val binding: ItemMyTicketBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemMyTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.binding.tvTicketTitle.text = ticket.title
        holder.binding.tvTicketId.text = "AG-IT-2024-${String.format("%06d", ticket.id)}"
        
        holder.binding.tvStatusBadge.text = ticket.status.uppercase()
        
        // Custom background and text color based on status
        when (ticket.status.lowercase()) {
            "open", "new" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                holder.binding.tvStatusBadge.setTextColor(holder.itemView.context.getColor(R.color.blue_600))
            }
            "in_progress", "assigned" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_in_progress)
                holder.binding.tvStatusBadge.setTextColor(holder.itemView.context.getColor(R.color.green_600))
            }
            "pending" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_medium)
                holder.binding.tvStatusBadge.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            "resolved", "closed" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                holder.binding.tvStatusBadge.setTextColor(holder.itemView.context.getColor(R.color.gray_500))
            }
        }

        holder.binding.btnStatus.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, com.simats.resolveiq_frontend.TicketProgressActivity::class.java).apply {
                putExtra("ticket_id", ticket.id)
                putExtra("ticket_title", ticket.title)
                putExtra("ticket_description", ticket.description)
                putExtra("ticket_status", ticket.status)
                putExtra("ticket_created_at", ticket.created_at)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.binding.btnViewDetails.setOnClickListener {
            onItemClick(ticket)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
}
