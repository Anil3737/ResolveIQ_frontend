package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.ActivitySeverity
import com.simats.resolveiq_frontend.data.model.ActivityType
import com.simats.resolveiq_frontend.data.model.AdminActivityLog
import com.simats.resolveiq_frontend.databinding.ItemAdminActivityBinding
import com.simats.resolveiq_frontend.utils.convertUtcToLocalShort

class AdminActivityAdapter(
    private var activities: List<AdminActivityLog>,
    private val onItemClick: (AdminActivityLog) -> Unit
) : RecyclerView.Adapter<AdminActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(private val binding: ItemAdminActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: AdminActivityLog) {
            binding.tvEventTitle.text = activity.action_type.replace("_", " ")
            binding.tvEventDescription.text = activity.description
            
            // Format timestamp: convert UTC ISO string to device local time
            binding.tvTimestamp.text = convertUtcToLocalShort(activity.created_at)
            
            binding.tvSeverityBadge.text = activity.role
            
            // Set icon based on action type
            val iconRes = when (activity.action_type) {
                "USER_CREATED", "USER_LOGIN" -> R.drawable.ic_user
                "TICKET_CREATED", "TICKET_ASSIGNED" -> R.drawable.ic_ticket
                "STATUS_UPDATED", "MANUAL_CLOSED", "AUTO_CLOSED" -> R.drawable.ic_history
                "AUTO_ESCALATED" -> R.drawable.ic_trend_up
                "SLA_BREACHED" -> R.drawable.ic_warning_amber
                else -> R.drawable.ic_history
            }
            binding.ivEventIcon.setImageResource(iconRes)

            // Set severity badge color based on action/role
            val badgeColor = when {
                activity.action_type == "SLA_BREACHED" || activity.action_type == "AUTO_ESCALATED" -> R.color.admin_red
                activity.action_type == "USER_CREATED" -> R.color.admin_green_accent
                else -> R.color.admin_blue_accent
            }
            binding.tvSeverityBadge.setBackgroundTintList(
                ContextCompat.getColorStateList(binding.root.context, badgeColor)
            )

            binding.root.setOnClickListener {
                onItemClick(activity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemAdminActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    fun updateData(newActivities: List<AdminActivityLog>) {
        activities = newActivities
        notifyDataSetChanged()
    }
}
