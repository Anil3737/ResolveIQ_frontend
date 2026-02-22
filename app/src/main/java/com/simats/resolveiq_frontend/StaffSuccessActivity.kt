package com.simats.resolveiq_frontend

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.databinding.ActivityStaffSuccessBinding

class StaffSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name") ?: "Staff Member"
        val email = intent.getStringExtra("email") ?: ""
        val role = intent.getStringExtra("role") ?: "STAFF"

        binding.tvName.text = name
        binding.tvEmail.text = email
        binding.tvRoleBadge.text = role.uppercase()
        binding.tvPassword.text = "Resolveiq@123"

        setupListeners(email)
    }

    private fun setupListeners(email: String) {
        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnCopyEmail.setOnClickListener {
            copyToClipboard("Email", email)
        }

        binding.btnCopyPassword.setOnClickListener {
            copyToClipboard("Password", "Resolveiq@123")
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
