package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.AdminActivityAdapter
import com.simats.resolveiq_frontend.data.model.ActivitySeverity
import com.simats.resolveiq_frontend.data.model.ActivityType
import com.simats.resolveiq_frontend.data.model.AdminActivityLog
import com.simats.resolveiq_frontend.databinding.ActivityAdminActivityLogBinding

class AdminActivityLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminActivityLogBinding
    private lateinit var activityAdapter: AdminActivityAdapter
    private val fullLogList = mutableListOf<AdminActivityLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupRecyclerView()
        loadSampleData()
        setupFilters()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivRefresh.setOnClickListener {
            loadSampleData()
        }
    }

    private fun setupRecyclerView() {
        activityAdapter = AdminActivityAdapter(fullLogList) { activity ->
            // Open details (read-only) - placeholder for now
        }
        binding.rvActivityLog.apply {
            layoutManager = LinearLayoutManager(this@AdminActivityLogActivity)
            adapter = activityAdapter
        }
    }

    private fun setupFilters() {
        binding.cgFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            val filterType = when (checkedIds.firstOrNull()) {
                R.id.chipAll -> ActivityType.ALL
                R.id.chipUserActions -> ActivityType.USER_ACTION
                R.id.chipEscalations -> ActivityType.ESCALATION
                R.id.chipSlaBreaches -> ActivityType.SLA_BREACH
                R.id.chipAiEvents -> ActivityType.AI_EVENT
                R.id.chipMajorIncidents -> ActivityType.MAJOR_INCIDENT
                else -> ActivityType.ALL
            }
            filterList(filterType)
        }
    }

    private fun filterList(type: ActivityType) {
        val filtered = if (type == ActivityType.ALL) {
            fullLogList
        } else {
            fullLogList.filter { it.type == type }
        }
        activityAdapter.updateData(filtered)
    }

    private fun loadSampleData() {
        fullLogList.clear()
        
        fullLogList.add(AdminActivityLog(
            "1", ActivityType.SLA_BREACH, "SLA Breach Risk exceeded 90%",
            "Network & VPN Team - Ticket #RIQ-0122", "2 mins ago", ActivitySeverity.CRITICAL
        ))
        
        fullLogList.add(AdminActivityLog(
            "2", ActivityType.USER_ACTION, "Admin created Team Lead Ravi Kumar",
            "Assigned to Infrastructure Department", "10 mins ago", ActivitySeverity.SUCCESS
        ))
        
        fullLogList.add(AdminActivityLog(
            "3", ActivityType.ESCALATION, "Ticket #EMP0023 escalated to Team Lead",
            "Triggered by high predicted delay", "1 hour ago", ActivitySeverity.WARNING
        ))
        
        fullLogList.add(AdminActivityLog(
            "4", ActivityType.AI_EVENT, "AI auto-reassigned ticket due to overload",
            "Rebalanced from Agent Sarah to Agent Mike", "Today, 10:45 AM", ActivitySeverity.INFO
        ))
        
        fullLogList.add(AdminActivityLog(
            "5", ActivityType.MAJOR_INCIDENT, "Major Incident: Primary DNS down",
            "System wide alert - DevOps team notified", "Today, 9:15 AM", ActivitySeverity.CRITICAL
        ))
        
        fullLogList.add(AdminActivityLog(
            "6", ActivityType.AI_EVENT, "AI Priority Score updated: #RIQ-5502",
            "Sentiment analyzed as 'Frustrated' - Score: 92", "Yesterday, 4:20 PM", ActivitySeverity.INFO
        ))
        
        fullLogList.add(AdminActivityLog(
            "7", ActivityType.USER_ACTION, "Updated escalation policy",
            "Global threshold changed from 85% to 80%", "Yesterday, 2:00 PM", ActivitySeverity.SUCCESS
        ))
        
        fullLogList.add(AdminActivityLog(
            "8", ActivityType.SLA_BREACH, "SLA Breached: #RIQ-9901",
            "Security Team - Response time exceeded", "12 Feb 2026", ActivitySeverity.CRITICAL
        ))

        activityAdapter.updateData(fullLogList)
        binding.chipAll.isChecked = true
    }
}
