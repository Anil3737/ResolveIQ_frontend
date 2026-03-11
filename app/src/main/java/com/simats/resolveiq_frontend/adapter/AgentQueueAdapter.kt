package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ItemAgentQueueTicketBinding

class AgentQueueAdapter(
    private var tickets: List<Ticket>,
    private val mode: Mode,
    private val onAccept: (Ticket) -> Unit,
    private val onResolve: (Ticket) -> Unit,
    private val onView: (Ticket) -> Unit
) : RecyclerView.Adapter<AgentQueueAdapter.ViewHolder>() {

    enum class Mode { POOL, ACTIVE, RESOLVED }

    class ViewHolder(val binding: ItemAgentQueueTicketBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAgentQueueTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]
        val ctx = holder.itemView.context

        // Ticket ID and title
        holder.binding.tvTicketId.text =
            ticket.ticket_number ?: "IQ-IT-2026-${String.format("%06d", ticket.id)}"
        holder.binding.tvTicketTitle.text = ticket.title

        // Status badge
        holder.binding.tvStatusBadge.text = ticket.status.uppercase()
        when (ticket.status.lowercase()) {
            "open", "new", "approved" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                holder.binding.tvStatusBadge.setTextColor(ctx.getColor(R.color.blue_600))
            }
            "in_progress", "assigned" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_in_progress)
                holder.binding.tvStatusBadge.setTextColor(ctx.getColor(R.color.green_600))
            }
            "resolved", "closed" -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new)
                holder.binding.tvStatusBadge.setTextColor(ctx.getColor(R.color.gray_500))
            }
            else -> {
                holder.binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_medium)
                holder.binding.tvStatusBadge.setTextColor(ctx.getColor(R.color.orange_500))
            }
        }

        // Priority badge
        holder.binding.tvPriorityBadge.text = ticket.priority.uppercase()
        when (ticket.priority.uppercase()) {
            "HIGH", "P1" -> {
                holder.binding.tvPriorityBadge.setBackgroundResource(R.drawable.bg_status_critical)
                holder.binding.tvPriorityBadge.setTextColor(ctx.getColor(R.color.red_600))
            }
            "MEDIUM", "P2" -> {
                holder.binding.tvPriorityBadge.setBackgroundResource(R.drawable.bg_status_medium)
                holder.binding.tvPriorityBadge.setTextColor(ctx.getColor(R.color.orange_500))
            }
            else -> {
                holder.binding.tvPriorityBadge.setBackgroundResource(R.drawable.bg_status_new)
                holder.binding.tvPriorityBadge.setTextColor(ctx.getColor(R.color.green_600))
            }
        }

        // Accepted at date for My Active tab
        if (mode == Mode.ACTIVE && ticket.accepted_at != null) {
            holder.binding.tvAcceptedAt.visibility = View.VISIBLE
            holder.binding.tvAcceptedAt.text = "Accepted: ${ticket.accepted_at.take(10)}"
        } else {
            holder.binding.tvAcceptedAt.visibility = View.GONE
        }

        // Show Accept button only in POOL mode when can_accept == true
        if (mode == Mode.POOL && ticket.can_accept == true) {
            holder.binding.btnAccept.visibility = View.VISIBLE
            holder.binding.btnAccept.setOnClickListener { onAccept(ticket) }
        } else {
            holder.binding.btnAccept.visibility = View.GONE
        }

        // Show Resolve button only in ACTIVE mode when can_resolve == true
        if (mode == Mode.ACTIVE && ticket.can_resolve == true) {
            holder.binding.btnResolve.visibility = View.VISIBLE
            holder.binding.btnResolve.setOnClickListener { onResolve(ticket) }
        } else {
            holder.binding.btnResolve.visibility = View.GONE
        }

        // View details button is always visible
        holder.binding.btnViewDetails.setOnClickListener { onView(ticket) }
        holder.itemView.setOnClickListener { onView(ticket) }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
}
