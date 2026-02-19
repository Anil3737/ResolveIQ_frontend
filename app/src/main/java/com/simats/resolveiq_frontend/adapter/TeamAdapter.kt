package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R

data class Team(
    val id: Int,
    val name: String,
    val category: String,
    val slaPercentage: String,
    val slaStatus: String,
    val leadName: String,
    val iconRes: Int,
    val iconBgColor: Int,
    val slaColor: Int,
    val slaBgColor: Int
)

class TeamAdapter(private val teams: List<Team>) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivTeamIcon: ImageView = view.findViewById(R.id.ivTeamIcon)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val tvTeamCategory: TextView = view.findViewById(R.id.tvTeamCategory)
        val tvSlaPercentage: TextView = view.findViewById(R.id.tvSlaPercentage)
        val tvSlaStatus: TextView = view.findViewById(R.id.tvSlaStatus)
        val tvLeadInfo: TextView = view.findViewById(R.id.tvLeadInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]
        holder.tvTeamName.text = team.name
        holder.tvTeamCategory.text = team.category
        holder.tvSlaPercentage.text = team.slaPercentage
        holder.tvSlaStatus.text = team.slaStatus
        holder.tvLeadInfo.text = "Lead: ${team.leadName}"
        
        holder.ivTeamIcon.setImageResource(team.iconRes)
        holder.ivTeamIcon.setBackgroundResource(R.drawable.bg_rounded_icon)
        holder.ivTeamIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(team.iconBgColor)
        
        holder.tvSlaPercentage.setTextColor(team.slaColor)
        holder.tvSlaPercentage.setBackgroundResource(team.slaBgColor)
    }

    override fun getItemCount() = teams.size
}
