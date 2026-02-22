package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.EmployeeAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.Employee
import com.simats.resolveiq_frontend.databinding.ActivityUsersListBinding
import kotlinx.coroutines.launch

class UsersListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersListBinding
    private lateinit var employeeAdapter: EmployeeAdapter
    private val employeeList = mutableListOf<Employee>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupRecyclerView()
        loadUsersFromApi()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                employeeAdapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter(employeeList) { employee ->
            val intent = Intent(this, ProfileInfoActivity::class.java)
            intent.putExtra("employeeId", employee.employeeId)
            startActivity(intent)
        }
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(this@UsersListActivity)
            adapter = employeeAdapter
        }
    }

    private fun loadUsersFromApi() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getAdminApi(this@UsersListActivity).getUsers()
                if (response.success && response.data != null) {
                    employeeList.clear()
                    response.data.forEach { user ->
                        employeeList.add(
                            Employee(
                                employeeId = user.phone ?: "N/A",
                                fullName = user.full_name,
                                role = user.role,
                                department = user.department_name
                            )
                        )
                    }
                    employeeAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@UsersListActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UsersListActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
