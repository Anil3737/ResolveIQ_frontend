package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.User
import com.simats.resolveiq_frontend.databinding.ActivityCreateAgentBinding
import kotlinx.coroutines.launch

class CreateAgentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAgentBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAgentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDropdowns()
        setupListeners()
    }


    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupDropdowns() {
        // Department options
        val departments = arrayOf(
            "Network Issue",
            "Hardware Failure",
            "Software Installation",
            "Application Downtime / Application Issues",
            "Other"
        )
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        binding.actDepartment.setAdapter(deptAdapter)

        // Location options
        val locations = arrayOf("Chennai HQ", "Bangalore", "Hyderabad")
        val locAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        binding.actLocation.setAdapter(locAdapter)
        
    }

    private fun setupListeners() {
        binding.actDepartment.setOnItemClickListener { _, _, _, _ ->
            // Department selected
        }

        binding.btnRegisterAgent.setOnClickListener {
            if (validateForm()) {
                val name = binding.etFullName.text.toString().trim()
                val empId = binding.etEmployeeId.text.toString().trim()
                val department = binding.actDepartment.text.toString()
                val location = binding.actLocation.text.toString()

                registerAgent(name, empId, department, location)
            }
        }
    }


    private fun validateForm(): Boolean {
        var isValid = true

        // 1. Full Name Validation
        val name = binding.etFullName.text.toString().trim()
        if (name.length < 3) {
            binding.tilFullName.error = "Name must be at least 3 characters"
            isValid = false
        } else if (!name.all { it.isLetter() || it.isWhitespace() }) {
            binding.tilFullName.error = "Only alphabets and spaces allowed"
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        // 2. EMP ID Validation
        val empId = binding.etEmployeeId.text.toString().trim().uppercase()
        val empIdRegex = Regex("^EMP\\d{4}$")
        
        if (!empId.startsWith("EMP")) {
            binding.tilEmployeeId.error = "Must start with 'EMP'"
            isValid = false
        } else if (empId.length != 7) {
            binding.tilEmployeeId.error = "Must be exactly 7 characters"
            isValid = false
        } else if (!empIdRegex.matches(empId)) {
            binding.tilEmployeeId.error = "Invalid format. Expected EMPXXXX"
            isValid = false
        } else {
            val numericPart = empId.substring(3).toIntOrNull()
            if (numericPart == null || numericPart !in 1000..2000) {
                binding.tilEmployeeId.error = "Numeric part must be between 1000 and 2000"
                isValid = false
            } else {
                // Example uniqueness check
                if (empId == "EMP1500") {
                    binding.tilEmployeeId.error = "Employee ID already exists"
                    isValid = false
                } else {
                    binding.tilEmployeeId.error = null
                }
            }
        }

        // 3. Department Validation
        if (binding.actDepartment.text.toString().isEmpty()) {
            binding.tilDepartment.error = "Required field"
            isValid = false
        } else {
            binding.tilDepartment.error = null
        }


        // 5. Location Validation
        if (binding.actLocation.text.toString().isEmpty()) {
            binding.tilLocation.error = "Required field"
            isValid = false
        } else {
            binding.tilLocation.error = null
        }

        return isValid
    }

    private fun registerAgent(name: String, empId: String, department: String, location: String) {
        val requestData = com.simats.resolveiq_frontend.data.model.CreateAgentRequest(
            full_name = name,
            emp_id = empId,
            department = department,
            team_lead_id = null,
            location = location
        )

        binding.btnRegisterAgent.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@CreateAgentActivity).createAgent(requestData)
                if (response.success) {
                    val intent = Intent(this@CreateAgentActivity, StaffSuccessActivity::class.java).apply {
                        putExtra("name", name)
                        putExtra("email", response.data?.email ?: "")
                        putExtra("role", "Support Agent")
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreateAgentActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                    binding.btnRegisterAgent.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateAgentActivity, "Network Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                binding.btnRegisterAgent.isEnabled = true
            }
        }
    }
}
