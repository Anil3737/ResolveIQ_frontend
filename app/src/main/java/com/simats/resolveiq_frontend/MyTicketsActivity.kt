package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.MyTicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityMyTicketsBinding
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MyTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyTicketsBinding
    private lateinit var adapter: MyTicketAdapter
    private lateinit var ticketRepository: TicketRepository
    private var allTickets: List<Ticket> = emptyList()
    private var isShowingActive = true
    private var fetchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupUI()
        fetchTickets()
    }

    private fun setupDependencies() {
        val api = RetrofitClient.getTicketApi(this)
        ticketRepository = TicketRepository(api)
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.rvMyTickets.layoutManager = LinearLayoutManager(this)
        adapter = MyTicketAdapter(emptyList()) { ticket ->
            val intent = android.content.Intent(this, TicketDetailsActivity::class.java).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvMyTickets.adapter = adapter

        binding.btnResolvedTab.setOnClickListener {
            updateTabs(isActive = false)
            filterTickets(isActive = false)
        }

        // Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_tickets
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = android.content.Intent(this, EmployeeHomeActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_tickets -> {
                    // Already on Tickets
                    true
                }
                R.id.nav_activity -> {
                    Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_settings -> {
                    val intent = android.content.Intent(this, SettingsActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_tickets
        fetchTickets()
    }

    private fun updateTabs(isActive: Boolean) {
        isShowingActive = isActive
        if (isActive) {
            binding.btnActiveTab.setBackgroundResource(R.drawable.button_primary)
            binding.btnActiveTab.backgroundTintList = getColorStateList(android.R.color.white)
            binding.btnActiveTab.setTextColor(getColor(R.color.blue_600))
            binding.btnActiveTab.setTypeface(null, android.graphics.Typeface.BOLD)

            binding.btnResolvedTab.background = null
            binding.btnResolvedTab.setTextColor(getColor(android.R.color.white))
            binding.btnResolvedTab.alpha = 0.7f
            binding.btnResolvedTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            binding.btnResolvedTab.setBackgroundResource(R.drawable.button_primary)
            binding.btnResolvedTab.backgroundTintList = getColorStateList(android.R.color.white)
            binding.btnResolvedTab.setTextColor(getColor(R.color.blue_600))
            binding.btnResolvedTab.setTypeface(null, android.graphics.Typeface.BOLD)

            binding.btnActiveTab.background = null
            binding.btnActiveTab.setTextColor(getColor(android.R.color.white))
            binding.btnActiveTab.alpha = 0.7f
            binding.btnActiveTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun fetchTickets() {
        fetchJob?.cancel()
        fetchJob = lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = ticketRepository.getTickets()
                if (result.isSuccess) {
                    allTickets = result.getOrDefault(emptyList())
                    filterTickets(isShowingActive)
                } else {
                    val error = result.exceptionOrNull()
                    if (error !is kotlinx.coroutines.CancellationException) {
                        Toast.makeText(this@MyTicketsActivity, "Failed to load tickets: ${error?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterTickets(isActive: Boolean) {
        val filtered = if (isActive) {
            allTickets.filter { it.status.lowercase() !in listOf("resolved", "closed") }
        } else {
            allTickets.filter { it.status.lowercase() in listOf("resolved", "closed") }
        }
        adapter.updateTickets(filtered)
        
        if (filtered.isEmpty()) {
            binding.tvNoTickets.visibility = View.VISIBLE
            binding.rvMyTickets.visibility = View.GONE
        } else {
            binding.tvNoTickets.visibility = View.GONE
            binding.rvMyTickets.visibility = View.VISIBLE
        }
    }
}

