package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.FeedbackRequest
import com.simats.resolveiq_frontend.databinding.ActivityFeedbackBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.launch

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var repository: TicketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TicketRepository(RetrofitClient.getTicketApi(this))

        val ticketId = intent.getIntExtra("ticket_id", -1)
        val ticketNumber = intent.getStringExtra("ticket_number") ?: "N/A"
        val ticketTitle = intent.getStringExtra("ticket_title") ?: "N/A"
        val isReadOnly = intent.getBooleanExtra("is_read_only", false)

        if (ticketId == -1) {
            Toast.makeText(this, "Invalid Ticket ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI(ticketNumber, ticketTitle, ticketId, isReadOnly)
    }

    private fun setupUI(ticketNumber: String, ticketTitle: String, ticketId: Int, isReadOnly: Boolean) {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvTicketInfo.text = "Ticket #$ticketNumber"
        binding.tvTicketTitle.text = ticketTitle

        if (isReadOnly) {
            enterReadOnlyMode(ticketId)
        } else {
            binding.btnSubmit.setOnClickListener {
                submitFeedback(ticketId)
            }
        }
    }

    private fun enterReadOnlyMode(ticketId: Int) {
        binding.layoutSubmittedBanner.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.GONE
        
        // Disable all inputs
        binding.ratingBar.setIsIndicator(true)
        binding.etComments.isEnabled = false
        binding.etComments.isFocusable = false
        
        // Disable all chips in the group
        for (i in 0 until binding.chipGroupSuggestions.childCount) {
            binding.chipGroupSuggestions.getChildAt(i).isEnabled = false
        }

        // Fetch and populate
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = repository.getFeedback(ticketId)
            binding.progressBar.visibility = View.GONE
            
            if (result.isSuccess) {
                val feedback = result.getOrNull()?.data
                if (feedback != null) {
                    populateFeedback(feedback)
                }
            }
        }
    }

    private fun populateFeedback(feedback: com.simats.resolveiq_frontend.data.model.FeedbackData) {
        binding.ratingBar.rating = feedback.rating.toFloat()
        binding.etComments.setText(feedback.comments)
        
        val suggestions = feedback.suggestions ?: emptyList()
        if (suggestions.contains("Fast Resolution")) binding.chipFastResolution.isChecked = true
        if (suggestions.contains("Great Communication")) binding.chipGreatCommunication.isChecked = true
        if (suggestions.contains("Professional Behavior")) binding.chipProfessionalBehavior.isChecked = true
        if (suggestions.contains("Technical Expertise")) binding.chipTechnicalExpertise.isChecked = true
        if (suggestions.contains("Excellent Service")) binding.chipExcellentService.isChecked = true
        if (suggestions.contains("Resolved on Time")) binding.chipResolvedOnTime.isChecked = true
        if (suggestions.contains("Very Helpful")) binding.chipVeryHelpful.isChecked = true
    }

    private fun submitFeedback(ticketId: Int) {
        val rating = binding.ratingBar.rating.toInt()
        val comments = binding.etComments.text.toString().trim()
        
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        val suggestions = mutableListOf<String>()
        if (binding.chipFastResolution.isChecked) suggestions.add("Fast Resolution")
        if (binding.chipGreatCommunication.isChecked) suggestions.add("Great Communication")
        if (binding.chipProfessionalBehavior.isChecked) suggestions.add("Professional Behavior")
        if (binding.chipTechnicalExpertise.isChecked) suggestions.add("Technical Expertise")
        if (binding.chipExcellentService.isChecked) suggestions.add("Excellent Service")
        if (binding.chipResolvedOnTime.isChecked) suggestions.add("Resolved on Time")
        if (binding.chipVeryHelpful.isChecked) suggestions.add("Very Helpful")

        val request = FeedbackRequest(
            rating = rating,
            comments = comments.ifEmpty { null },
            suggestions = if (suggestions.isNotEmpty()) suggestions else null
        )

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSubmit.isEnabled = false
            
            val result = repository.submitFeedback(ticketId, request)
            
            binding.progressBar.visibility = View.GONE
            binding.btnSubmit.isEnabled = true

            if (result.isSuccess) {
                Toast.makeText(this@FeedbackActivity, "Feedback submitted successfully!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this@FeedbackActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
