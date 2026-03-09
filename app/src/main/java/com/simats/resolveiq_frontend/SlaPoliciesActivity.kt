package com.simats.resolveiq_frontend

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.api.SlaApiService
import com.simats.resolveiq_frontend.data.model.*
import com.simats.resolveiq_frontend.databinding.ActivitySlaPoliciesBinding
import com.simats.resolveiq_frontend.databinding.DialogConfigureSlaBinding
import com.simats.resolveiq_frontend.databinding.ItemSlaRuleBinding
import kotlinx.coroutines.launch

class SlaPoliciesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySlaPoliciesBinding
    private lateinit var slaApiService: SlaApiService
    private lateinit var adminApiService: com.simats.resolveiq_frontend.api.AdminApiService
    private val slaAdapter = SlaAdapter()
    
    private var departments = listOf<Department>()
    private val priorities = listOf("CRITICAL", "HIGH", "MEDIUM", "LOW")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlaPoliciesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        slaApiService = RetrofitClient.getSlaApi(this)
        adminApiService = RetrofitClient.getAdminApi(this)

        setupToolbar()
        setupRecyclerView()
        fetchData()

        binding.fabAddRule.setOnClickListener {
            showConfigureRuleDialog(null)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvSlaRules.apply {
            layoutManager = LinearLayoutManager(this@SlaPoliciesActivity)
            adapter = slaAdapter
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            try {
                // Fetch departments for the spinners
                val deptResponse = adminApiService.getDepartments()
                if (deptResponse.success) {
                    departments = deptResponse.data ?: emptyList()
                }

                // Fetch existing SLA rules
                fetchRules()
            } catch (e: Exception) {
                Toast.makeText(this@SlaPoliciesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun fetchRules() {
        try {
            val response = slaApiService.getRules()
            if (response.isSuccessful && response.body()?.success == true) {
                val rules = response.body()?.data ?: emptyList()
                slaAdapter.submitList(rules)
                binding.tvRuleCount.text = "${rules.size} Rules"
                binding.llEmptyState.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(this@SlaPoliciesActivity, "Failed to load rules", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfigureRuleDialog(rule: SlaRule?) {
        val dialogBinding = DialogConfigureSlaBinding.inflate(layoutInflater)
        val isEdit = rule != null

        // Setup Spinners
        val deptNames = departments.map { it.name }
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, deptNames)
        dialogBinding.spinnerDepartment.setAdapter(deptAdapter)

        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priorities)
        dialogBinding.spinnerPriority.setAdapter(priorityAdapter)

        // Pre-fill if editing
        if (isEdit) {
            dialogBinding.spinnerDepartment.setText(rule?.department_name, false)
            dialogBinding.spinnerDepartment.isEnabled = false // Cannot change dept/priority for existing rule (based on backend logic usually)
            dialogBinding.spinnerPriority.setText(rule?.priority, false)
            dialogBinding.spinnerPriority.isEnabled = false
            dialogBinding.etSlaHours.setText(rule?.sla_hours.toString())
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (isEdit) "Update SLA Rule" else "Create SLA Rule")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (isEdit) "Update" else "Create") { _, _ ->
                val selectedDeptName = dialogBinding.spinnerDepartment.text.toString()
                val selectedPriority = dialogBinding.spinnerPriority.text.toString()
                val hoursStr = dialogBinding.etSlaHours.text.toString()

                if (selectedDeptName.isEmpty() || selectedPriority.isEmpty() || hoursStr.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val hours = hoursStr.toIntOrNull() ?: 0
                val deptId = departments.find { it.name == selectedDeptName }?.id ?: 0

                if (isEdit) {
                    updateRule(rule!!.id, hours)
                } else {
                    createRule(deptId, selectedPriority, hours)
                }
            }
            .show()
    }

    private fun createRule(deptId: Int, priority: String, hours: Int) {
        lifecycleScope.launch {
            try {
                val response = slaApiService.createRule(SlaRuleRequest(deptId, priority, hours))
                if (response.isSuccessful) {
                    Toast.makeText(this@SlaPoliciesActivity, "Rule created", Toast.LENGTH_SHORT).show()
                    fetchRules()
                } else {
                    Toast.makeText(this@SlaPoliciesActivity, "Failed to create rule", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SlaPoliciesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRule(id: Int, hours: Int) {
        lifecycleScope.launch {
            try {
                val response = slaApiService.updateRule(id, SlaRuleUpdateRequest(hours))
                if (response.isSuccessful) {
                    Toast.makeText(this@SlaPoliciesActivity, "Rule updated", Toast.LENGTH_SHORT).show()
                    fetchRules()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SlaPoliciesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteRule(id: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Rule")
            .setMessage("Are you sure you want to delete this SLA policy?")
            .setNegativeButton("No", null)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = slaApiService.deleteRule(id)
                        if (response.isSuccessful) {
                            Toast.makeText(this@SlaPoliciesActivity, "Rule deleted", Toast.LENGTH_SHORT).show()
                            fetchRules()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SlaPoliciesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    inner class SlaAdapter : RecyclerView.Adapter<SlaAdapter.ViewHolder>() {
        private var items = listOf<SlaRule>()

        fun submitList(newList: List<SlaRule>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSlaRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvDepartmentName.text = item.department_name ?: "Department #${item.department_id}"
            holder.binding.tvPriorityLabel.text = "Priority: ${item.priority}"
            holder.binding.tvSlaHours.text = "\u23F1\uFE0F ${item.sla_hours} Hours"
            
            holder.binding.tvPriorityBadge.text = item.priority.take(1)
            val color = when(item.priority) {
                "CRITICAL" -> "#EF4444"
                "HIGH" -> "#F97316"
                "MEDIUM" -> "#FBBF24"
                else -> "#34D399"
            }
            holder.binding.tvPriorityBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))

            holder.binding.btnEditRule.setOnClickListener { showConfigureRuleDialog(item) }
            holder.binding.btnDeleteRule.setOnClickListener { deleteRule(item.id) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: ItemSlaRuleBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
