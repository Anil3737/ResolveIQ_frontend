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
import com.simats.resolveiq_frontend.adapter.AgentQueueAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.databinding.ActivityAssignedTicketsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.launch

class AssignedTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignedTicketsBinding
    private lateinit var adapter: AgentQueueAdapter
    private lateinit var repository: TicketRepository
    private lateinit var userPreferences: UserPreferences

    private var allTickets: List<Ticket> = emptyList()
    private var currentTab: AgentQueueAdapter.Mode = AgentQueueAdapter.Mode.POOL
    private var currentSearch: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignedTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        repository = TicketRepository(RetrofitClient.getTicketApi(this))

        setupUI()
        setupBottomNavigation()
        fetchTickets()
    }

    override fun onResume() {
        super.onResume()
        // No item selected in bottom nav by default for this specialized activity
        binding.bottomNavigation.selectedItemId = 0 
        fetchTickets()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, SupportAgentHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tickets -> {
                    startActivity(Intent(this, MyTicketsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_activity -> {
                    startActivity(Intent(this, AgentPerformanceActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Search logic
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearch = s?.toString() ?: ""
                filterAndApply()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tab logic
        binding.tabPool.setOnClickListener { updateTab(AgentQueueAdapter.Mode.POOL) }
        binding.tabActive.setOnClickListener { updateTab(AgentQueueAdapter.Mode.ACTIVE) }
        binding.tabResolved.setOnClickListener { updateTab(AgentQueueAdapter.Mode.RESOLVED) }

        // Recycler view
        binding.rvAssignedTickets.layoutManager = LinearLayoutManager(this)
        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = AgentQueueAdapter(
            tickets = emptyList(),
            mode = currentTab,
            onAccept = { handleAction(it, "ACCEPT") },
            onResolve = { handleAction(it, "RESOLVED") },
            onView = { 
                val intent = Intent(this, TicketDetailsActivity::class.java).apply {
                    putExtra("ticket", it)
                }
                startActivity(intent)
            }
        )
        binding.rvAssignedTickets.adapter = adapter
    }

    private fun updateTab(newMode: AgentQueueAdapter.Mode) {
        if (currentTab == newMode) return
        currentTab = newMode
        
        // Update tab UI colors
        val selectedBg = R.drawable.bg_tab_selected
        val transparent = android.R.color.transparent
        
        binding.tabPool.setBackgroundResource(if (newMode == AgentQueueAdapter.Mode.POOL) selectedBg else transparent)
        binding.tabActive.setBackgroundResource(if (newMode == AgentQueueAdapter.Mode.ACTIVE) selectedBg else transparent)
        binding.tabResolved.setBackgroundResource(if (newMode == AgentQueueAdapter.Mode.RESOLVED) selectedBg else transparent)

        setupAdapter() // Re-setup adapter with new mode for conditional buttons
        filterAndApply()
    }

    private fun fetchTickets() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = repository.getAgentTickets(this@AssignedTicketsActivity)
                if (result.isSuccess) {
                    allTickets = result.getOrDefault(emptyList())
                    
                    // Fallback: If agent-specific endpoint returns nothing, try generic fetch
                    if (allTickets.isEmpty()) {
                        val fallbackResult = repository.getTickets()
                        if (fallbackResult.isSuccess) {
                            allTickets = fallbackResult.getOrDefault(emptyList())
                        }
                    }
                    
                    if (allTickets.isNotEmpty()) {
                        Toast.makeText(this@AssignedTicketsActivity, "Fetched ${allTickets.size} tickets", Toast.LENGTH_SHORT).show()
                    }
                    filterAndApply()
                } else {
                    // Try fallback immediately on direct failure
                    val fallbackResult = repository.getTickets()
                    if (fallbackResult.isSuccess) {
                        allTickets = fallbackResult.getOrDefault(emptyList())
                        filterAndApply()
                    } else {
                        Toast.makeText(this@AssignedTicketsActivity, "Failed to load tickets", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssignedTicketsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterAndApply() {
        val currentUserId = userPreferences.getUserId()
        
        val filtered = allTickets.filter { t ->
            // Search filter
            val matchesSearch = currentSearch.isEmpty() || 
                t.title.contains(currentSearch, ignoreCase = true) ||
                t.ticket_number?.contains(currentSearch, ignoreCase = true) == true

            // Tab filter (following web app logic)
            val matchesTab = when (currentTab) {
                AgentQueueAdapter.Mode.POOL -> {
                    // Pool: can_accept is true OR (Assigned to me but not yet accepted)
                    // OR (Unassigned and status is APPROVED/OPEN)
                    t.can_accept == true || 
                    (t.assigned_to == currentUserId && t.accepted_at == null) ||
                    (t.assigned_to == null && (t.status.equals("APPROVED", true) || t.status.equals("OPEN", true)))
                }
                AgentQueueAdapter.Mode.ACTIVE -> {
                    t.assigned_to == currentUserId && t.accepted_at != null && !t.status.equals("RESOLVED", true) && !t.status.equals("CLOSED", true)
                }
                AgentQueueAdapter.Mode.RESOLVED -> {
                    t.assigned_to == currentUserId && (t.status.equals("RESOLVED", true) || t.status.equals("CLOSED", true))
                }
            }
            
            matchesSearch && matchesTab
        }

        // Debug Toast: Only for development, but helpful for user to confirm data arrival
        if (allTickets.isNotEmpty() && filtered.isEmpty()) {
             // If we have tickets but none matched the current tab, show a helpful message
             // Toast.makeText(this, "Fetched ${allTickets.size} total, but none match this tab", Toast.LENGTH_SHORT).show()
        }

        adapter.updateTickets(filtered)
        
        if (filtered.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvAssignedTickets.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvAssignedTickets.visibility = View.VISIBLE
        }
    }

    private fun handleAction(ticket: Ticket, action: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = repository.updateTicketAction(this@AssignedTicketsActivity, ticket.id, action)
                if (result.isSuccess) {
                    Toast.makeText(this@AssignedTicketsActivity, "Ticket ${action.lowercase()}ed successfully", Toast.LENGTH_SHORT).show()
                    fetchTickets() // Refresh list
                } else {
                    Toast.makeText(this@AssignedTicketsActivity, "Failed to $action ticket", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssignedTicketsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
