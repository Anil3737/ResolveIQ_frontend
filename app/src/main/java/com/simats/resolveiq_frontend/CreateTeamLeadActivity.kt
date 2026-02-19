package com.simats.resolveiq_frontend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityCreateTeamLeadBinding

class CreateTeamLeadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTeamLeadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTeamLeadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreateLeadAccount.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Backend logic will be added later
            Toast.makeText(this, "Team Lead Account Created for $name", Toast.LENGTH_LONG).show()
            finish()
        }

        // Dummy listener for radio group items to make them feel interactive
        binding.rbActivationLink.setOnClickListener { binding.rbActivationLink.isChecked = true }
        binding.rbTempPassword.setOnClickListener { binding.rbTempPassword.isChecked = true }
    }
}
