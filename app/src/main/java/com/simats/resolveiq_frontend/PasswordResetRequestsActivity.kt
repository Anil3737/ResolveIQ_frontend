package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.AdminPasswordResetRequest
import com.simats.resolveiq_frontend.data.model.ApproveResetRequest
import com.simats.resolveiq_frontend.data.model.RejectResetRequest
import kotlinx.coroutines.launch

class PasswordResetRequestsActivity : AppCompatActivity() {

    private lateinit var rvRequests: RecyclerView
    private lateinit var adapter: PasswordResetRequestsAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var llNoRequests: LinearLayout
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset_requests)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadRequests()
    }

    private fun initializeViews() {
        rvRequests = findViewById(R.id.rvRequests)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        llNoRequests = findViewById(R.id.llNoRequests)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        adapter = PasswordResetRequestsAdapter(
            requests = emptyList(),
            onApprove = { approveRequest(it) },
            onReject = { rejectRequest(it) }
        )
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        swipeRefresh.setOnRefreshListener { loadRequests() }
    }

    private fun loadRequests() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val adminApi = RetrofitClient.getAdminApi(this@PasswordResetRequestsActivity)
                val response = adminApi.getPasswordResetRequests("PENDING")

                if (response.success) {
                    val requests = response.data ?: emptyList()
                    adapter.updateData(requests)
                    llNoRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@PasswordResetRequestsActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PasswordResetRequestsActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun approveRequest(request: AdminPasswordResetRequest) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val adminApi = RetrofitClient.getAdminApi(this@PasswordResetRequestsActivity)
                val response = adminApi.approvePasswordReset(ApproveResetRequest(request.id))

                if (response.success) {
                    Toast.makeText(this@PasswordResetRequestsActivity, response.message, Toast.LENGTH_LONG).show()
                    loadRequests()
                } else {
                    Toast.makeText(this@PasswordResetRequestsActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PasswordResetRequestsActivity, "Approval failed", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun rejectRequest(request: AdminPasswordResetRequest) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val adminApi = RetrofitClient.getAdminApi(this@PasswordResetRequestsActivity)
                val response = adminApi.rejectPasswordReset(RejectResetRequest(request.id))

                if (response.success) {
                    Toast.makeText(this@PasswordResetRequestsActivity, response.message, Toast.LENGTH_LONG).show()
                    loadRequests()
                } else {
                    Toast.makeText(this@PasswordResetRequestsActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PasswordResetRequestsActivity, "Rejection failed", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
