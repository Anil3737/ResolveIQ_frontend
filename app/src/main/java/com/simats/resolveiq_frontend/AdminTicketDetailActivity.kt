package com.simats.resolveiq_frontend

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ActivityAdminTicketDetailBinding
import com.simats.resolveiq_frontend.utils.convertUtcToLocal
import java.text.SimpleDateFormat
import java.util.*

class AdminTicketDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminTicketDetailBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminTicketDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ticket = intent.getSerializableExtra("ticket") as? Ticket
        if (ticket == null) {
            finish()
            return
        }

        setupUI(ticket)
        startCountdown(ticket)
    }

    private fun setupUI(ticket: Ticket) {
        binding.ivBack.setOnClickListener { finish() }

        binding.tvHeaderTitle.text = ticket.ticket_number ?: "Ticket #${ticket.id}"
        binding.tvIssueType.text = ticket.department_name ?: "ISSUE"
        binding.tvTitle.text = ticket.title
        binding.tvTicketCreatedOn.text = "Created on: ${convertUtcToLocal(ticket.created_at)}"
        
        // Extract location from description
        val locationRegex = "Location: (.*?)\\n".toRegex()
        val matchResult = locationRegex.find(ticket.description)
        val extractedLocation = matchResult?.groupValues?.get(1) ?: "N/A"
        
        val cleanDescription = if (matchResult != null) {
            ticket.description.replaceFirst("Location: .*?\\n\\n".toRegex(), "")
        } else {
            ticket.description
        }

        binding.tvLocation.text = extractedLocation
        binding.tvDescription.text = cleanDescription

        // Employee Details
        binding.tvEmployeeName.text = ticket.created_by_name ?: "Unknown Employee"
        binding.tvEmployeeId.text = "EMP ID: ${ticket.created_by_emp_id ?: "N/A"}"
    }

    private fun startCountdown(ticket: Ticket) {
        val deadlineStr = ticket.sla_deadline
        if (deadlineStr.isNullOrBlank()) {
            binding.tvRemainingTime.text = "--:--:--"
            binding.tvSlaStatus.text = "No SLA set"
            return
        }

        val deadlineDate = parseIso8601(deadlineStr)
        if (deadlineDate == null) {
            binding.tvRemainingTime.text = "--:--:--"
            return
        }

        val currentTime = System.currentTimeMillis()
        val diff = deadlineDate.time - currentTime

        if (diff <= 0 || ticket.status in listOf("RESOLVED", "CLOSED")) {
            binding.tvRemainingTime.text = "00:00:00"
            binding.tvSlaStatus.text = if (ticket.status in listOf("RESOLVED", "CLOSED")) "Completed" else "SLA Breached"
            binding.tvSlaStatus.setTextColor(Color.parseColor("#EF4444")) // Red
            return
        }

        countDownTimer = object : CountDownTimer(diff, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (1000 * 60 * 60)
                val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (millisUntilFinished % (1000 * 60)) / 1000
                
                binding.tvRemainingTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                
                // Color change based on urgency
                if (hours < 1) {
                    binding.tvSlaStatus.setTextColor(Color.parseColor("#F59E0B")) // Amber
                    binding.tvSlaStatus.text = "Nearing Breach"
                }
            }

            override fun onFinish() {
                binding.tvRemainingTime.text = "00:00:00"
                binding.tvSlaStatus.text = "SLA Breached"
                binding.tvSlaStatus.setTextColor(Color.parseColor("#EF4444"))
            }
        }.start()
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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
