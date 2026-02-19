package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.MyTicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityEmployeeHomeBinding
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EmployeeHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeHomeBinding
    private lateinit var adapter: MyTicketAdapter
    private lateinit var userPreferences: UserPreferences
    
    private lateinit var authRepository: AuthRepository
    private lateinit var ticketRepository: TicketRepository
    private var fetchJob: Job? = null

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
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }
        
        binding.navView.menuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
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
        adapter = MyTicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, TicketDetailsActivity::class.java).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvRecentTickets.adapter = adapter
        
        // Action Row
        binding.btnCreateTicket.setOnClickListener {
            startActivity(Intent(this, CreateTicketActivity::class.java))
        }
        
        binding.btnMyTickets.setOnClickListener {
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }

        // Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on Home
                    true
                }
                R.id.nav_tickets -> {
                    val intent = Intent(this, MyTicketsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_activity -> {
                    Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        fetchJob?.cancel()
        fetchJob = lifecycleScope.launch {
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
                // Limit to 3 most recent
                adapter.updateTickets(tickets.take(3))
                binding.tvWelcomeSubtitle.text = "You have ${tickets.size} total tickets"
                
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
    
    

}
