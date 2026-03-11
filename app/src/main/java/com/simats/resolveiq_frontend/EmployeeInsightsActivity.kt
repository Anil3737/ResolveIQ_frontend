package com.simats.resolveiq_frontend

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.AnalyticsSummary
import com.simats.resolveiq_frontend.data.model.TrendAnalytics
import com.simats.resolveiq_frontend.databinding.ActivityEmployeeInsightsBinding
import com.simats.resolveiq_frontend.repository.AnalyticsRepository
import kotlinx.coroutines.launch

class EmployeeInsightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeInsightsBinding
    private lateinit var repository: AnalyticsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val api = RetrofitClient.getAnalyticsApi(this)
        repository = AnalyticsRepository(api)

        setupUI()
        fetchData()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivRefresh.setOnClickListener { fetchData() }
        
        // Configure Pie Chart
        binding.pieChartStatus.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setCenterText("Status")
            setCenterTextSize(14f)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
        }

        // Configure Line Chart
        binding.lineChartTrend.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
        }
    }

    private fun fetchData() {
        setLoading(true)
        lifecycleScope.launch {
            val summaryResult = repository.getSummary()
            val trendResult = repository.getTrend(14)
            val slaResult = repository.getSLACompliance()

            setLoading(false)

            if (summaryResult.isSuccess && trendResult.isSuccess) {
                val summary = summaryResult.getOrNull()
                val trend = trendResult.getOrNull()
                val sla = slaResult.getOrNull()

                if (summary != null && trend != null) {
                    binding.emptyLayout.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    
                    updateMetrics(summary, sla?.compliance_pct ?: 0.0)
                    renderPieChart(summary)
                    renderLineChart(trend)
                } else {
                    showEmptyState()
                }
            } else {
                Toast.makeText(this@EmployeeInsightsActivity, "Failed to load insights", Toast.LENGTH_SHORT).show()
                showEmptyState()
            }
        }
    }

    private fun updateMetrics(summary: AnalyticsSummary, compliance: Double) {
        binding.tvTotalTickets.text = summary.total_tickets.toString()
        binding.tvSlaCompliance.text = "${compliance}%"
        
        // Dynamic color for compliance
        binding.tvSlaCompliance.setTextColor(when {
            compliance >= 90 -> Color.parseColor("#10B981") // Green
            compliance >= 70 -> Color.parseColor("#F59E0B") // Amber
            else -> Color.parseColor("#EF4444") // Red
        })
    }

    private fun renderPieChart(summary: AnalyticsSummary) {
        val entries = mutableListOf<PieEntry>()
        summary.status_summary.forEach { (status, count) ->
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), status))
            }
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        binding.pieChartStatus.data = data
        binding.pieChartStatus.invalidate()
    }

    private fun renderLineChart(trend: List<TrendAnalytics>) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        trend.forEachIndexed { index, item ->
            entries.add(Entry(index.toFloat(), item.tickets.toFloat()))
            // Format date string (e.g., 2026-03-11 -> 03/11)
            val dateParts = item.date.split("-")
            if (dateParts.size >= 3) {
                labels.add("${dateParts[1]}/${dateParts[2]}")
            } else {
                labels.add(item.date)
            }
        }

        val dataSet = LineDataSet(entries, "Tickets Created")
        dataSet.color = Color.parseColor("#2563EB")
        dataSet.setCircleColor(Color.parseColor("#2563EB"))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#2563EB")
        dataSet.fillAlpha = 50

        val data = LineData(dataSet)
        binding.lineChartTrend.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChartTrend.xAxis.granularity = 1f
        binding.lineChartTrend.data = data
        binding.lineChartTrend.invalidate()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState() {
        binding.contentLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.VISIBLE
    }
}
