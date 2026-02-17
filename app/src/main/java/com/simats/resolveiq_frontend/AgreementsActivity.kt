package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityAgreementsBinding

class AgreementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgreementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAccept.setOnClickListener {
            // Simply close and return to register activity
            finish()
        }

        binding.btnDecline.setOnClickListener {
            // Close the screen
            finish()
        }
    }
}
