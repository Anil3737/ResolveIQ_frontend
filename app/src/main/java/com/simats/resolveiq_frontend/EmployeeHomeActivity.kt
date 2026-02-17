package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.TicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityEmployeeHomeBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class EmployeeHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeHomeBinding
    private lateinit var adapter: TicketAdapter
    private lateinit var userPreferences: UserPreferences
    
    private lateinit var authRepository: AuthRepository
    private lateinit var ticketRepository: TicketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        initializeDependencies()
        setupUI()
        
        fetchDashboardData()
    }
    
    private fun initializeDependencies() {
        val authApi = RetrofitClient.getAuthApi(this)
        val ticketApi = RetrofitClient.getTicketApi(this)
        
        authRepository = AuthRepository(authApi)
        ticketRepository = TicketRepository(ticketApi)
    }
    
    private fun setupUI() {
        // Set initial welcome name from preferences
        val storedName = userPreferences.getUserName() ?: "Employee"
        binding.tvWelcomeUser.text = "Welcome back, $storedName!"

        // Navigation Drawer
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        
        binding.navView.ivCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Drawer Menu Items
        binding.navView.menuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        
        binding.navView.menuCreateTicket.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, CreateTicketActivity::class.java))
        }

        binding.navView.menuMyTickets.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            fetchDashboardData()
        }
        
        binding.navView.menuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Logout
        binding.navView.menuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            performLogout()
        }

        // Profile Icon click
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // RecyclerView
        binding.rvRecentTickets.layoutManager = LinearLayoutManager(this)
        adapter = TicketAdapter(emptyList()) { ticket ->
            Toast.makeText(this, "Clicked: ${ticket.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvRecentTickets.adapter = adapter
        
        // Action Row
        binding.btnCreateTicket.setOnClickListener {
            startActivity(Intent(this, CreateTicketActivity::class.java))
        }
        
        binding.btnMyTickets.setOnClickListener {
            Toast.makeText(this, "Showing My Tickets", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDashboardData() {
        lifecycleScope.launch {
            // Fetch User
            val userResult = authRepository.getCurrentUser()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                user?.let {
                    binding.tvWelcomeUser.text = "Welcome back, ${it.full_name}!"
                    userPreferences.saveUserName(it.full_name)
                    userPreferences.saveUserId(it.id)
                    userPreferences.saveUserRole(it.role)
                }
            }

            // Fetch Tickets
            val ticketsResult = ticketRepository.getTickets()
            if (ticketsResult.isSuccess) {
                val tickets = ticketsResult.getOrDefault(emptyList())
                adapter.updateTickets(tickets)
                binding.tvWelcomeSubtitle.text = "You have ${tickets.size} recent tickets"
                
                // Update Stats
                binding.tvOpenCount.text = tickets.count { it.status.equals("open", true) }.toString()
                binding.tvPendingCount.text = tickets.count { it.status.equals("pending", true) }.toString()
                binding.tvResolvedCount.text = tickets.count { it.status.equals("resolved", true) || it.status.equals("closed", true) }.toString()
            } else {
                Toast.makeText(this@EmployeeHomeActivity, "Failed to load tickets", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun performLogout() {
        userPreferences.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        fetchDashboardData()
    }
}
