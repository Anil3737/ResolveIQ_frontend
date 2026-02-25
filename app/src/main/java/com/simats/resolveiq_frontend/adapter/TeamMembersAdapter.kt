package com.simats.resolveiq_frontend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.TeamMember
import com.simats.resolveiq_frontend.databinding.ItemTeamMemberBinding

class TeamMembersAdapter(private val context: Context) :
    RecyclerView.Adapter<TeamMembersAdapter.TeamMemberViewHolder>() {

    private var members: List<TeamMember> = emptyList()

    fun setMembers(newMembers: List<TeamMember>) {
        members = newMembers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
        val binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return TeamMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    inner class TeamMemberViewHolder(private val binding: ItemTeamMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(member: TeamMember) {
            binding.tvAgentName.text = member.full_name
            binding.tvLocation.text = member.location
            binding.tvDepartment.text = member.department
            
            val active = member.active_tickets
            val capacity = member.daily_capacity
            binding.tvWorkloadStatus.text = "Tickets Today: $active / $capacity"
            
            // Progress Calculation
            val progress = ((active.toFloat() / capacity.toFloat()) * 100).toInt()
            binding.progressWorkload.progress = progress
            
            // Overloaded Badge
            binding.chipOverloaded.visibility = if (active >= 12) View.VISIBLE else View.GONE
            
            // Color Logic
            val colorRes = when {
                active <= 5 -> {
                    binding.tvLoadLabel.text = "Low Load"
                    R.color.green_600
                }
                active <= 10 -> {
                    binding.tvLoadLabel.text = "Medium Load"
                    R.color.orange_500
                }
                else -> {
                    binding.tvLoadLabel.text = "High Load"
                    R.color.admin_red
                }
            }
            
            val color = ContextCompat.getColor(context, colorRes)
            binding.tvLoadLabel.setTextColor(color)
            binding.progressWorkload.setIndicatorColor(color)
        }
    }
}
