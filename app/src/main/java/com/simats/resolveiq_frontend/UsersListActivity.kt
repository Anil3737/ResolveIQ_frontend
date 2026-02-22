package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.EmployeeAdapter
import com.simats.resolveiq_frontend.data.model.Employee
import com.simats.resolveiq_frontend.databinding.ActivityUsersListBinding

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
        loadDummyData()
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
            // Use numeric ID part if possible, or just pass as String
            // The user asked to pass "employee ID as extra"
            intent.putExtra("employeeId", employee.employeeId)
            startActivity(intent)
        }
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(this@UsersListActivity)
            adapter = employeeAdapter
        }
    }

    private fun loadDummyData() {
        employeeList.clear()
        employeeList.add(Employee("EMP0001", "Alex Johnson", "Senior Developer", "Engineering"))
        employeeList.add(Employee("EMP0002", "Sarah Smith", "Product Manager", "Products"))
        employeeList.add(Employee("EMP0003", "Michael Chen", "UI Designer", "Design"))
        employeeList.add(Employee("EMP0004", "Jessica Brown", "HR Manager", "People"))
        employeeList.add(Employee("EMP0005", "Daniel Lee", "QA Engineer", "Engineering"))
        employeeList.add(Employee("EMP0006", "Emily Davis", "Marketing Lead", "Growth"))
        employeeList.add(Employee("EMP0007", "Robert Wilson", "Security Analyst", "Operations"))
        employeeList.add(Employee("EMP0008", "Sophia Martinez", "Project Coordinator", "Products"))
        
        employeeAdapter.notifyDataSetChanged()
    }
}
