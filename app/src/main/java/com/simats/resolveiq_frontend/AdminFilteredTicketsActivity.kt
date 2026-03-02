package com.simats.resolveiq_frontend

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.MyTicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ActivityAdminFilteredTicketsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.launch

class AdminFilteredTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminFilteredTicketsBinding
    private lateinit var adapter: MyTicketAdapter
    private lateinit var ticketRepository: TicketRepository
    private var departmentName: String? = null
    private var allFilteredTickets: List<Ticket> = emptyList()
    private var isShowingActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminFilteredTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        departmentName = intent.getStringExtra("department_name")
        binding.tvHeaderTitle.text = departmentName ?: "Tickets"

        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)

        setupRecyclerView()
        setupListeners()
        setupBottomNavigation()
        fetchTickets()
    }

    private fun setupRecyclerView() {
        binding.rvTickets.layoutManager = LinearLayoutManager(this)
        adapter = MyTicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, AdminTicketDetailActivity::class.java)
            intent.putExtra("ticket", ticket)
            startActivity(intent)
        }
        binding.rvTickets.adapter = adapter
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnActiveTab.setOnClickListener {
            if (!isShowingActive) {
                isShowingActive = true
                updateTabsUI()
                applyLocalFilter()
            }
        }

        binding.btnResolvedTab.setOnClickListener {
            if (isShowingActive) {
                isShowingActive = false
                updateTabsUI()
                applyLocalFilter()
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            fetchTickets()
        }
    }

    private fun updateTabsUI() {
        if (isShowingActive) {
            binding.btnActiveTab.setBackgroundResource(R.drawable.button_primary)
            binding.btnActiveTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6")))
            binding.btnActiveTab.setTextColor(Color.WHITE)
            binding.btnActiveTab.setTypeface(null, Typeface.BOLD)

            binding.btnResolvedTab.setBackgroundResource(0)
            binding.btnResolvedTab.setTextColor(resources.getColor(R.color.text_secondary, theme))
            binding.btnResolvedTab.setTypeface(null, Typeface.NORMAL)
        } else {
            binding.btnResolvedTab.setBackgroundResource(R.drawable.button_primary)
            binding.btnResolvedTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6")))
            binding.btnResolvedTab.setTextColor(Color.WHITE)
            binding.btnResolvedTab.setTypeface(null, Typeface.BOLD)

            binding.btnActiveTab.setBackgroundResource(0)
            binding.btnActiveTab.setTextColor(resources.getColor(R.color.text_secondary, theme))
            binding.btnActiveTab.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun fetchTickets() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = true
                val result = ticketRepository.getTickets()
                if (result.isSuccess) {
                    val rawTickets = result.getOrDefault(emptyList())
                    
                    // Filter initially by department name (extracted tag) - This is the "Single source of truth"
                    allFilteredTickets = rawTickets.filter { ticket ->
                        val title = ticket.title
                        val extractedTag = if (title.startsWith("[") && title.contains("]")) {
                            title.substring(1, title.indexOf("]")).trim()
                        } else {
                            ticket.department_name?.trim()?.takeIf { it.isNotEmpty() } ?: "Other"
                        }
                        extractedTag == departmentName
                    }
                    
                    applyLocalFilter()
                } else {
                    Toast.makeText(this@AdminFilteredTicketsActivity, "Failed to load tickets", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminFilteredTicketsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun applyLocalFilter() {
        // No additional API calls - Filtering from the already loaded 'allFilteredTickets'
        val filtered = allFilteredTickets.filter { ticket ->
            val status = ticket.status.lowercase()
            if (isShowingActive) {
                status !in listOf("resolved", "closed")
            } else {
                status in listOf("resolved", "closed")
            }
        }
        
        adapter.updateTickets(filtered)
        binding.tvNoTickets.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_admin_tickets
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> {
                    val intent = Intent(this, AdminHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_admin_tickets -> {
                    val intent = Intent(this, AdminGroupedTicketsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_admin_users -> {
                    startActivity(Intent(this, UsersListActivity::class.java))
                    true
                }
                R.id.nav_admin_activity -> {
                    startActivity(Intent(this, AdminActivityLogActivity::class.java))
                    true
                }
                R.id.nav_admin_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
