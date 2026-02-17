package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.CreateTicketRequest
import com.simats.resolveiq_frontend.databinding.ActivityCreateTicketBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class CreateTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTicketBinding
    private lateinit var ticketRepository: TicketRepository
    
    // Hardcoded issue types for demo
    private val issueTypes = listOf("Network Issue", "Hardware Failure", "Software Installation", "Application Downtime / Application Issues", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupUI()
    }

    private fun setupDependencies() {
        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
    }

    private fun setupUI() {
        // Back Button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Issue Type Dropdown
        binding.layoutIssueType.setOnClickListener {
            showIssueTypeDialog()
        }

        // Submit Button
        binding.btnSubmit.setOnClickListener {
            submitTicket()
        }
    }
    
    private fun showIssueTypeDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Issue Type")
        builder.setItems(issueTypes.toTypedArray()) { _, which ->
            binding.tvIssueType.text = issueTypes[which]
            binding.tvIssueType.setTextColor(resources.getColor(R.color.black, theme))
        }
        builder.show()
    }

    private fun submitTicket() {
        val issueType = binding.tvIssueType.text.toString()
        val location = binding.etLocation.text.toString()
        val description = binding.etDescription.text.toString()
        // Resolution time ignored as per strict backend contract
        
        if (issueType == "Choose an issue type..." || issueType.isBlank()) {
            Toast.makeText(this, "Please select an issue type", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (location.isBlank()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (description.isBlank()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Combine Location into Title and Description
        val fullTitle = "[$issueType] at $location"
        val fullDescription = "Location: $location\n\n$description"
        
        // Hardcoded Department ID = 1 (IT)
        val departmentId = 1
        
        lifecycleScope.launch {
            binding.btnSubmit.isEnabled = false
            
            val request = CreateTicketRequest(
                title = fullTitle,
                description = fullDescription,
                departmentId = departmentId
            )

            val result = ticketRepository.createTicket(request)
            
            if (result.isSuccess) {
                // Navigate to Success Screen
                startActivity(Intent(this@CreateTicketActivity, TicketSuccessActivity::class.java))
                finish()
            } else {
                binding.btnSubmit.isEnabled = true
                Toast.makeText(this@CreateTicketActivity, "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
