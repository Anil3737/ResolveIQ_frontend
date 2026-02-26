package com.simats.resolveiq_frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.TicketAdapter
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.databinding.ActivityTeamLeadTicketsBinding
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.launch

class TeamLeadTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamLeadTicketsBinding
    private lateinit var repository: TicketRepository
    private lateinit var adapter: TicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamLeadTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TicketRepository(RetrofitClient.getTicketApi(this))
        setupRecyclerView()
        setupToolbar()
        loadTickets()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = TicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, TicketDetailsActivity::class.java).apply {
                putExtra("ticket", ticket)
            }
            startActivity(intent)
        }
        binding.rvTickets.layoutManager = LinearLayoutManager(this)
        binding.rvTickets.adapter = adapter
    }

    private fun loadTickets() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
            
            val result = repository.getTickets()
            binding.progressBar.visibility = View.GONE
            
            if (result.isSuccess) {
                val tickets = result.getOrNull() ?: emptyList()
                adapter.updateTickets(tickets)
                if (tickets.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            } else {
                binding.tvEmpty.text = "Error loading tickets: ${result.exceptionOrNull()?.message}"
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTickets()
    }
}
