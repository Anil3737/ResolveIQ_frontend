package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.TeamData

// Keep for backward compatibility
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

class TeamAdapter(
    private val teams: List<TeamData>,
    private val onItemClick: (TeamData) -> Unit
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivTeamIcon: ImageView = view.findViewById(R.id.ivTeamIcon)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val tvTeamCategory: TextView = view.findViewById(R.id.tvTeamCategory)
        val tvSlaPercentage: TextView = view.findViewById(R.id.tvSlaPercentage)
        val tvSlaStatus: TextView = view.findViewById(R.id.tvSlaStatus)
        val tvLeadInfo: TextView = view.findViewById(R.id.tvLeadInfo)
        val ivLeadAvatar: ImageView = view.findViewById(R.id.ivLeadAvatar)
    }

    private var currentTeams: List<TeamData> = teams

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = currentTeams[position]
        holder.tvTeamName.text = team.name
        holder.tvTeamCategory.text = team.department.uppercase()
        
        // Show department as a tag/badge on the right
        holder.tvSlaPercentage.text = team.department.uppercase()
        holder.tvSlaPercentage.setBackgroundResource(R.drawable.bg_status_open)
        
        // Show creation date at the bottom right
        val date = if (team.created_at.length >= 10) team.created_at.substring(0, 10) else team.created_at
        holder.tvSlaStatus.text = date
        
        // Show lead info
        holder.tvLeadInfo.text = team.team_lead
        
        // Set team icon based on department (simplified)
        holder.ivTeamIcon.setImageResource(R.drawable.ic_team_group)
        
        holder.itemView.setOnClickListener {
            onItemClick(team)
        }
    }

    fun updateData(newTeams: List<TeamData>) {
        currentTeams = newTeams
        notifyDataSetChanged()
    }

    override fun getItemCount() = currentTeams.size
}
