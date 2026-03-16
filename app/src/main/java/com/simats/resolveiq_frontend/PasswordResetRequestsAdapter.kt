package com.simats.resolveiq_frontend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.data.model.AdminPasswordResetRequest

class PasswordResetRequestsAdapter(
    private var requests: List<AdminPasswordResetRequest>,
    private val onApprove: (AdminPasswordResetRequest) -> Unit,
    private val onReject: (AdminPasswordResetRequest) -> Unit
) : RecyclerView.Adapter<PasswordResetRequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvEmpId: TextView = view.findViewById(R.id.tvEmpId)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password_reset_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.tvEmail.text = request.email
        holder.tvEmpId.text = "EMP ID: ${request.emp_id}"

        holder.btnApprove.setOnClickListener { onApprove(request) }
        holder.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<AdminPasswordResetRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
