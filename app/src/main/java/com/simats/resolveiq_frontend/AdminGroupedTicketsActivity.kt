package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.GroupedTicketAdapter
import com.simats.resolveiq_frontend.adapter.TicketGroup
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityAdminGroupedTicketsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class AdminGroupedTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminGroupedTicketsBinding
    private lateinit var adapter: GroupedTicketAdapter
    private lateinit var ticketRepository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminGroupedTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupUI()
        fetchAndGroupTickets()
    }

    private fun setupDependencies() {
        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
        userPreferences = UserPreferences(this)
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.rvGroupedTickets.layoutManager = LinearLayoutManager(this)
        adapter = GroupedTicketAdapter(emptyList()) { departmentName ->
            val intent = Intent(this, AdminFilteredTicketsActivity::class.java).apply {
                putExtra("department_name", departmentName)
            }
            startActivity(intent)
        }
        binding.rvGroupedTickets.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            fetchAndGroupTickets()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_admin_tickets
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> {
                    val intent = Intent(this, AdminHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_admin_tickets -> true
                R.id.nav_admin_users -> {
                    startActivity(Intent(this, UsersListActivity::class.java))
                    true
                }
                R.id.nav_admin_activity -> {
                    startActivity(Intent(this, AdminActivityLogActivity::class.java))
                    true
                }
                R.id.nav_admin_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchAndGroupTickets() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = true
                
                val result = ticketRepository.getTickets()
                if (result.isSuccess) {
                    val allTickets = result.getOrDefault(emptyList())
                    
                    // Group by Issue Type extracted from Title e.g. "[Network Issue] at Location"
                    val groups = allTickets.groupBy { ticket ->
                        val title = ticket.title
                        if (title.startsWith("[") && title.contains("]")) {
                            title.substring(1, title.indexOf("]")).trim()
                        } else {
                            ticket.department_name?.trim()?.takeIf { it.isNotEmpty() } ?: "Other"
                        }
                    }.map { (name, tickets) -> TicketGroup(name, tickets.size) }
                    .sortedBy { it.departmentName }

                    adapter.updateData(groups)
                    
                    if (groups.isEmpty()) {
                        binding.tvNoData.visibility = View.VISIBLE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@AdminGroupedTicketsActivity, "Failed to load tickets", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminGroupedTicketsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_admin_tickets
        fetchAndGroupTickets()
    }
}
