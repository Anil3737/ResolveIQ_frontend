package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.AdminActivityAdapter
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.ActivityType
import com.simats.resolveiq_frontend.data.model.AdminActivityLog
import com.simats.resolveiq_frontend.databinding.ActivityAdminActivityLogBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.simats.resolveiq_frontend.data.model.ActivityLogResponse

class AdminActivityLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminActivityLogBinding
    private lateinit var activityAdapter: AdminActivityAdapter
    private lateinit var adminApiService: com.simats.resolveiq_frontend.api.AdminApiService
    private val fullLogList = mutableListOf<AdminActivityLog>()
    private var currentPage = 1
    private val limit = 10
    private var isLastPage = false
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminApiService = com.simats.resolveiq_frontend.api.RetrofitClient.getAdminApi(this)

        setupListeners()
        setupRecyclerView()
        setupFilters()
        fetchActivityLogs(true)
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivRefresh.setOnClickListener {
            fetchActivityLogs(true)
        }
    }

    private fun setupRecyclerView() {
        activityAdapter = AdminActivityAdapter(fullLogList) { activity ->
            // Details placeholder
        }
        binding.rvActivityLog.apply {
            layoutManager = LinearLayoutManager(this@AdminActivityLogActivity)
            adapter = activityAdapter
            
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                            fetchActivityLogs(false)
                        }
                    }
                }
            })
        }
    }

    private fun setupFilters() {
        binding.cgFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            fetchActivityLogs(true)
        }
    }

    private fun getSelectedActionType(): String? {
        return when (binding.cgFilters.checkedChipId) {
            R.id.chipUserActions -> "USER_CREATED" // Simplified for now, or multiple
            R.id.chipEscalations -> "AUTO_ESCALATED"
            R.id.chipSlaBreaches -> "SLA_BREACHED"
            else -> null
        }
    }

    private fun fetchActivityLogs(refresh: Boolean) {
        if (refresh) {
            currentPage = 1
            isLastPage = false
        }
        
        isLoading = true
        val actionType = getSelectedActionType()

        lifecycleScope.launchWhenStarted {
            try {
                val response = adminApiService.getSystemActivity(
                    page = currentPage,
                    limit = limit,
                    actionType = actionType
                )
                
                if (response.success) {
                    if (refresh) fullLogList.clear()
                    
                    val newLogs = response.logs
                    fullLogList.addAll(newLogs)
                    activityAdapter.updateData(fullLogList.toList())
                    
                    isLastPage = newLogs.size < limit
                    if (!isLastPage) currentPage++
                } else {
                    android.widget.Toast.makeText(this@AdminActivityLogActivity, response.message ?: "Failed to fetch logs", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@AdminActivityLogActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }
}
