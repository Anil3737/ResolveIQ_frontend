package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityTicketSuccessBinding

class TicketSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnCheckProgress.setOnClickListener {
            // Navigate back to Home (User can see tickets there)
            // Or explicitly to My Tickets logic if separate
            val intent = Intent(this, EmployeeHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
