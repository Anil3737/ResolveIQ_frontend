package com.simats.resolveiq_frontend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityCreateTeamBinding

class CreateTeamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTeamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvSelectSpecialty.setOnClickListener {
            showIssueTypePicker()
        }

        binding.btnSubmitCreateTeam.setOnClickListener {
            val teamName = binding.etTeamName.text.toString().trim()
            val specialty = binding.tvSelectSpecialty.text.toString()
            
            if (teamName.isEmpty()) {
                Toast.makeText(this, "Please enter team name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (specialty == "Select specialty") {
                Toast.makeText(this, "Please select an issue type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Backend part will be done later as per request
            Toast.makeText(this, "Team '$teamName' Created Successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showIssueTypePicker() {
        val specialties = arrayOf("Network Support", "Infrastructure", "Security", "Hardware", "Database Management")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Specialty")
        builder.setItems(specialties) { _, which ->
            binding.tvSelectSpecialty.text = specialties[which]
        }
        builder.show()
    }
}
