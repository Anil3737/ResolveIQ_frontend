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

class AdminActivityAdapter(
    private var activities: List<AdminActivityLog>,
    private val onItemClick: (AdminActivityLog) -> Unit
) : RecyclerView.Adapter<AdminActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(private val binding: ItemAdminActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: AdminActivityLog) {
            binding.tvEventTitle.text = activity.title
            binding.tvEventDescription.text = activity.description
            binding.tvTimestamp.text = activity.timestamp
            binding.tvSeverityBadge.text = activity.severity.name

            // Set icon based on activity type
            val iconRes = when (activity.type) {
                ActivityType.USER_ACTION -> R.drawable.ic_user
                ActivityType.ESCALATION -> R.drawable.ic_trend_up
                ActivityType.SLA_BREACH -> R.drawable.ic_warning_amber
                ActivityType.AI_EVENT -> R.drawable.ic_brain_chip
                ActivityType.MAJOR_INCIDENT -> R.drawable.ic_announcement
                else -> R.drawable.ic_history
            }
            binding.ivEventIcon.setImageResource(iconRes)

            // Set severity badge color
            val badgeColor = when (activity.severity) {
                ActivitySeverity.INFO -> R.color.admin_blue_accent
                ActivitySeverity.WARNING -> R.color.admin_amber
                ActivitySeverity.CRITICAL -> R.color.admin_red
                ActivitySeverity.SUCCESS -> R.color.admin_green_accent
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
