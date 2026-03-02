package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R

data class TicketGroup(
    val departmentName: String,
    val ticketCount: Int
)

class GroupedTicketAdapter(
    private var groups: List<TicketGroup>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GroupedTicketAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDeptName: TextView = view.findViewById(R.id.tvDeptName)
        val tvTicketCount: TextView = view.findViewById(R.id.tvTicketCount)
        val tvDeptInitial: TextView = view.findViewById(R.id.tvDeptInitial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grouped_department, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.tvDeptName.text = group.departmentName
        holder.tvTicketCount.text = "${group.ticketCount} Tickets"
        holder.tvDeptInitial.text = group.departmentName.take(1).uppercase()
        
        holder.itemView.setOnClickListener {
            onItemClick(group.departmentName)
        }
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<TicketGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
