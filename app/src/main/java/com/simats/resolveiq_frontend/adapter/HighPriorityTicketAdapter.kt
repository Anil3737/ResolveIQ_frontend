package com.simats.resolveiq_frontend.adapter

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ItemHighPriorityTicketBinding
import java.text.SimpleDateFormat
import java.util.*

class HighPriorityTicketAdapter(
    private var tickets: List<Ticket>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<HighPriorityTicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(val binding: ItemHighPriorityTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        var timer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemHighPriorityTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        
        holder.binding.tvTicketNumber.text = ticket.ticket_number ?: "TIC-${ticket.id}"
        holder.binding.tvTitle.text = ticket.title
        holder.binding.tvDescription.text = ticket.description
        holder.binding.tvDepartment.text = ticket.department_name ?: "ISSUE"

        // Priority Badge & Text
        val priority = ticket.priority.uppercase()
        holder.binding.tvPriorityBadge.text = priority
        when (priority) {
            "P1" -> {
                holder.binding.tvPriorityBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.blue_600)))
                holder.binding.tvPriorityText.text = "CRITICAL"
                holder.binding.tvPriorityText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.red_100)))
                holder.binding.tvPriorityText.setTextColor(holder.itemView.context.getColor(R.color.red_600))
            }
            "P2" -> {
                holder.binding.tvPriorityBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.orange_500)))
                holder.binding.tvPriorityText.text = "HIGH"
                holder.binding.tvPriorityText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.orange_100)))
                holder.binding.tvPriorityText.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            else -> {
                holder.binding.tvPriorityBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.gray_500)))
                holder.binding.tvPriorityText.text = "MEDIUM"
                holder.binding.tvPriorityText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.gray_100)))
                holder.binding.tvPriorityText.setTextColor(holder.itemView.context.getColor(R.color.gray_500))
            }
        }

        // Risk Badge
        val riskValue = ticket.ai_score ?: ticket.breach_risk
        if (riskValue != null) {
            holder.binding.tvRiskBadge.visibility = View.VISIBLE
            holder.binding.tvRiskBadge.text = "$riskValue RISK"
        } else {
            holder.binding.tvRiskBadge.visibility = View.GONE
        }

        // SLA Countdown
        holder.timer?.cancel()
        val deadline = ticket.sla_deadline
        if (!deadline.isNullOrBlank()) {
            val deadlineDate = parseIso8601(deadline)
            if (deadlineDate != null) {
                val diff = deadlineDate.time - System.currentTimeMillis()
                if (diff > 0) {
                    holder.binding.llCountdown.visibility = View.VISIBLE
                    holder.timer = object : CountDownTimer(diff, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val h = millisUntilFinished / (1000 * 60 * 60)
                            val m = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                            val s = (millisUntilFinished % (1000 * 60)) / 1000
                            holder.binding.tvCountdown.text = String.format("%02d:%02d:%02d", h, m, s)
                        }
                        override fun onFinish() {
                            holder.binding.tvCountdown.text = "BREACHED"
                        }
                    }.start()
                } else {
                    holder.binding.llCountdown.visibility = View.VISIBLE
                    holder.binding.tvCountdown.text = "BREACHED"
                }
            } else {
                holder.binding.llCountdown.visibility = View.GONE
            }
        } else {
            holder.binding.llCountdown.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(ticket) }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }

    private fun parseIso8601(timestamp: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssZ"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(timestamp)
            } catch (_: Exception) {}
        }
        return null
    }
}
