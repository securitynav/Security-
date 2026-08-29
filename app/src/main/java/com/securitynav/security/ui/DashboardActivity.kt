package com.securitynav.security.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.securitynav.security.R
import com.securitynav.security.data.database.SecurityDatabase

class DashboardActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var tvLogsSummary: TextView
    private lateinit var tvLogsDetail: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadMetricsAndRefreshCharts()
            handler.postDelayed(this, 2000)
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvLogsSummary = findViewById(R.id.tvLogsSummary)
        tvLogsDetail = findViewById(R.id.tvLogsDetail)
        lineChart = findViewById(R.id.lineChartTraffic)
        pieChart = findViewById(R.id.pieChartMethods)

        setupLineChart()
        setupPieChart()
    }

    protected override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    protected override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(Color.parseColor("#121212"))
            legend.textColor = Color.WHITE

            xAxis.apply {
                textColor = Color.LTGRAY
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            axisLeft.apply {
                textColor = Color.LTGRAY
                setDrawGridLines(true)
                gridColor = Color.parseColor("#333333")
            }
            axisRight.isEnabled = false
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(Color.parseColor("#121212"))
            setTransparentCircleColor(Color.TRANSPARENT)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)
            legend.textColor = Color.WHITE
        }
    }

    private fun loadMetricsAndRefreshCharts() {
        try {
            val dbHelper = SecurityDatabase(this)
            val db = dbHelper.getReadableEncryptedDatabase()
            val cursor = db.rawQuery(
                "SELECT timestamp, event_type, details FROM security_logs ORDER BY id DESC LIMIT 100",
                null
            )

            val builder = StringBuilder()
            var totalEvents = 0
            var getCount = 0
            var postCount = 0
            var otherCount = 0

            val trafficEntries = ArrayList<Entry>()
            var timeIndex = 0f

            while (cursor.moveToNext()) {
                totalEvents++
                val time = cursor.getLong(0)
                val type = cursor.getString(1)
                val details = cursor.getString(2)

                when {
                    details.contains("[GET]") -> getCount++
                    details.contains("[POST]") -> postCount++
                    else -> otherCount++
                }

                val payloadSize = parsePayloadBytes(details)
                trafficEntries.add(Entry(timeIndex, payloadSize))
                timeIndex += 1.0f

                if (totalEvents <= 20) {
                    builder.append("[$time] $type:\n$details\n-------------------------------\n")
                }
            }
            cursor.close()
            db.close()

            tvLogsSummary.text = "Eventos Totales: $totalEvents | GET: $getCount | POST: $postCount | Otros: $otherCount"
            tvLogsDetail.text = if (builder.isNotEmpty()) builder.toString() else "Esperando capturas..."

            updateLineChartData(trafficEntries.reversed())
            updatePieChartData(getCount, postCount, otherCount)

        } catch (e: Exception) {
            tvLogsSummary.text = "Error al actualizar métricas"
            tvLogsDetail.text = e.localizedMessage
        }
    }

    private fun parsePayloadBytes(details: String): Float {
        val regex = Regex("Payload:\\s*(\\d+)B")
        val match = regex.find(details)
        return match?.groupValues?.get(1)?.toFloatOrNull() ?: 64f
    }

    private fun updateLineChartData(entries: List<Entry>) {
        if (entries.isEmpty()) return

        val dataSet = LineDataSet(entries, "Ancho de Banda (Bytes)").apply {
            color = Color.parseColor("#00FF66")
            setCircleColor(Color.parseColor("#00E5FF"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextColor = Color.WHITE
            valueTextSize = 8f
            setDrawFilled(true)
            fillColor = Color.parseColor("#00FF66")
            fillAlpha = 40
        }

        lineChart.data = LineData(dataSet)
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun updatePieChartData(get: Int, post: Int, other: Int) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (get > 0) {
            entries.add(PieEntry(get.toFloat(), "GET"))
            colors.add(Color.parseColor("#00E5FF"))
        }
        if (post > 0) {
            entries.add(PieEntry(post.toFloat(), "POST"))
            colors.add(Color.parseColor("#FFD600"))
        }
        if (other > 0) {
            entries.add(PieEntry(other.toFloat(), "Otros"))
            colors.add(Color.parseColor("#FF4444"))
        }

        if (entries.isEmpty()) return

        val dataSet = PieDataSet(entries, "Protocolos").apply {
            this.colors = colors
            sliceSpace = 3f
            valueTextColor = Color.BLACK
            valueTextSize = 11f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
            setValueTextColor(Color.WHITE)
        }

        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }
}
