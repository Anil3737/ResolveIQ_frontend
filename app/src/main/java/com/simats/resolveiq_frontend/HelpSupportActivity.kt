package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityHelpSupportBinding

class HelpSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        // FAQ 1 Toggle
        binding.layoutFaq1Header.setOnClickListener {
            toggleFaq(binding.tvFaq1Answer, binding.ivFaq1Arrow)
        }

        // FAQ 2 Toggle
        binding.layoutFaq2Header.setOnClickListener {
            toggleFaq(binding.tvFaq2Answer, binding.ivFaq2Arrow)
        }

        // FAQ 3 Toggle
        binding.layoutFaq3Header.setOnClickListener {
            toggleFaq(binding.tvFaq3Answer, binding.ivFaq3Arrow)
        }

        // Create Support Ticket button
        binding.btnCreateTicket.setOnClickListener {
            val intent = Intent(this, CreateTicketActivity::class.java)
            startActivity(intent)
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            // Placeholder for Privacy Policy
        }

        binding.tvTermsOfService.setOnClickListener {
            // Placeholder for Terms of Service
        }
    }

    private fun toggleFaq(answerView: TextView, arrowIcon: ImageView) {
        if (answerView.visibility == View.VISIBLE) {
            answerView.visibility = View.GONE
            arrowIcon.animate().rotation(0f).setDuration(200).start()
        } else {
            answerView.visibility = View.VISIBLE
            arrowIcon.animate().rotation(180f).setDuration(200).start()
        }
    }
}
