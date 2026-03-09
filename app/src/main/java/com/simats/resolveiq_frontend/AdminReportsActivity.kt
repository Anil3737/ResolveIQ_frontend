package com.simats.resolveiq_frontend

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.simats.resolveiq_frontend.api.AnalyticsApiService
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.data.model.*
import com.simats.resolveiq_frontend.databinding.ActivityAdminReportsBinding
import com.simats.resolveiq_frontend.databinding.ItemAgentPerformanceBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminReportsBinding
    private lateinit var analyticsApiService: AnalyticsApiService
    private val agentAdapter = AgentPerformanceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        analyticsApiService = RetrofitClient.getAnalyticsApi(this)

        setupToolbar()
        setupRecyclerView()
        setupChartStyles()
        fetchAnalytics()

        binding.btnRefresh.setOnClickListener {
            fetchAnalytics()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvAgentPerformance.apply {
            layoutManager = LinearLayoutManager(this@AdminReportsActivity)
            adapter = agentAdapter
        }
    }

    private fun setupChartStyles() {
        // Line Chart
        binding.trendChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            legend.isEnabled = false
        }

        // Horizontal Bar Chart — fixed to show labels on Y axis (departments)
        binding.departmentChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.setDrawLabels(true)
            xAxis.textSize = 10f
            axisLeft.setDrawGridLines(false)
            axisLeft.isEnabled = true
            axisRight.isEnabled = false
            legend.isEnabled = true
            legend.form = Legend.LegendForm.SQUARE
            legend.textSize = 12f
            setFitBars(true)
            // Enough bottom padding for labels
            setExtraBottomOffset(10f)
        }

        // Pie Chart
        binding.feedbackPieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            setHoleColor(Color.WHITE)
            setDrawEntryLabels(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.DKGRAY)
            legend.isEnabled = true
            legend.form = Legend.LegendForm.CIRCLE
        }
    }

    private fun fetchAnalytics() {
        binding.tvLastRefreshed.text = "Syncing live data..."

        lifecycleScope.launch {
            try {
                val summaryDef = async { analyticsApiService.getSummary() }
                val deptDef = async { analyticsApiService.getByDepartment() }
                val trendDef = async { analyticsApiService.getTrend() }
                val agentDef = async { analyticsApiService.getAgentPerformance() }
                val slaDef = async { analyticsApiService.getSLACompliance() }
                val feedbackDef = async {
                    try { analyticsApiService.getFeedbackSummary() } catch (e: Exception) { null }
                }

                val summary = summaryDef.await()
                val depts = deptDef.await()
                val trend = trendDef.await()
                val agents = agentDef.await()
                val sla = slaDef.await()
                val feedback = feedbackDef.await()

                if (summary.success) updateSummaryUI(summary.data)
                if (trend.success) updateTrendChart(trend.data)
                if (depts.success) updateDepartmentChart(depts.data)
                if (agents.success) agentAdapter.submitList(agents.data)
                if (sla.success) updateSLAUI(sla.data)
                if (feedback != null && feedback.success) updateFeedbackChart(feedback.data)

                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                binding.tvLastRefreshed.text = "Last updated: $currentTime"

            } catch (e: Exception) {
                Toast.makeText(this@AdminReportsActivity, "Sync failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.tvLastRefreshed.text = "Sync failed at ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
            }
        }
    }

    private fun updateSummaryUI(summary: AnalyticsSummary) {
        binding.tvTotalTicketsCount.text = summary.total_tickets.toString()
        // Count both RESOLVED and CLOSED as "resolved" tickets
        val resolved = (summary.status_summary["RESOLVED"] ?: 0) + (summary.status_summary["CLOSED"] ?: 0)
        binding.tvResolvedTicketsCount.text = resolved.toString()
        binding.tvOpenTicketsCount.text = summary.status_summary["OPEN"]?.toString() ?: "0"
    }

    private fun updateSLAUI(sla: SLAAnalytics) {
        binding.tvSlaCompliance.text = "${sla.compliance_pct}%"
    }

    private fun updateTrendChart(trendData: List<TrendAnalytics>) {
        val entries = trendData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.tickets.toFloat())
        }

        val dataSet = LineDataSet(entries, "Tickets").apply {
            color = getColor(R.color.purple_600)
            setCircleColor(getColor(R.color.purple_600))
            lineWidth = 3f
            circleRadius = 4f
            setDrawCircleHole(true)
            valueTextSize = 10f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = getColor(R.color.purple_50)
            fillAlpha = 50
        }

        binding.trendChart.data = LineData(dataSet)
        binding.trendChart.xAxis.valueFormatter = IndexAxisValueFormatter(trendData.map { it.date.takeLast(5) })
        binding.trendChart.invalidate()
    }

    private fun updateDepartmentChart(deptData: List<DepartmentAnalytics>) {
        // FIX: Use clearly distinct colors for Active (orange) and Resolved (green)
        val activeColor  = Color.parseColor("#F97316") // Orange
        val resolvedColor = Color.parseColor("#10B981") // Green

        val entries = deptData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), floatArrayOf(data.open.toFloat(), data.resolved.toFloat()))
        }

        val dataSet = BarDataSet(entries, "").apply {
            stackLabels = arrayOf("Active", "Resolved")
            colors = listOf(activeColor, resolvedColor)
            setDrawValues(false)
        }

        val labels = deptData.map { dept ->
            // Truncate long department names for readability
            if (dept.department.length > 15) dept.department.take(12) + "…" else dept.department
        }

        val data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.departmentChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            xAxis.labelRotationAngle = -30f
            setVisibleXRangeMaximum(labels.size.toFloat())
            // Increase height dynamically based on count
            val minHeight = maxOf(300, deptData.size * 55)
            layoutParams = layoutParams.apply { height = resources.displayMetrics.density.toInt() * minHeight }
            invalidate()
        }
    }

    private fun updateFeedbackChart(feedback: FeedbackSummary) {
        binding.tvAvgRating.text = "⭐ Average Rating: ${feedback.avg_rating} / 5"
        binding.tvTotalFeedbacks.text = "Total Submissions: ${feedback.total_feedbacks}"

        if (feedback.total_feedbacks == 0) {
            binding.feedbackPieChart.setNoDataText("No feedback submitted yet")
            binding.feedbackPieChart.invalidate()
            return
        }

        val starColors = listOf(
            Color.parseColor("#EF4444"), // 1 star - Red
            Color.parseColor("#F97316"), // 2 stars - Orange
            Color.parseColor("#FBBF24"), // 3 stars - Amber
            Color.parseColor("#34D399"), // 4 stars - Green
            Color.parseColor("#4F46E5")  // 5 stars - Indigo
        )

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        for (i in 1..5) {
            val count = feedback.rating_distribution[i.toString()] ?: 0
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), "$i ★"))
                colors.add(starColors[i - 1])
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 3f
            valueTextSize = 13f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(binding.feedbackPieChart)
        }

        binding.feedbackPieChart.apply {
            this.data = PieData(dataSet)
            animateY(800)
            invalidate()
        }
    }

    inner class AgentPerformanceAdapter : RecyclerView.Adapter<AgentPerformanceAdapter.ViewHolder>() {
        private var items = listOf<AgentPerformance>()

        fun submitList(newList: List<AgentPerformance>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAgentPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvAgentName.text = item.agent
            holder.binding.tvAgentEmpId.text = "Emp ID: ${item.emp_id}"
            holder.binding.tvResolutionRate.text = "${item.resolution_rate}%"
            holder.binding.tvAgentInitials.text = item.agent.split(" ").map { it.take(1) }.joinToString("").uppercase()
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: ItemAgentPerformanceBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
