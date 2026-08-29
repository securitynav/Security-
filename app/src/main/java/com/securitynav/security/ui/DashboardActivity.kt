package com.securitynav.security.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.securitynav.security.R
import com.securitynav.security.data.AppTrafficItem
import com.securitynav.security.notifications.SecurityNotificationManager
import java.util.Random

class DashboardActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var rvAppTraffic: RecyclerView
    private val entries = ArrayList<Entry>()
    private val appList = ArrayList<AppTrafficItem>()
    private lateinit var adapter: AppTrafficAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var timeX = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val chartContainer = findViewById<FrameLayout>(R.id.chartContainer)
        rvAppTraffic = findViewById(R.id.rvAppTraffic)

        // Crear LineChart programáticamente
        lineChart = LineChart(this)
        chartContainer.addView(lineChart)

        setupChart()
        setupRecyclerView()
        startLiveDataSimulation()
    }

    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setBackgroundColor(Color.parseColor("#161D2F"))

        val dataSet = LineDataSet(entries, "Tráfico KB/s").apply {
            color = Color.parseColor("#00E5FF")
            valueTextColor = Color.WHITE
            lineWidth = 2.5f
            setDrawCircles(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        lineChart.data = LineData(dataSet)
    }

    private fun setupRecyclerView() {
        appList.add(AppTrafficItem("WhatsApp", "com.whatsapp", "142 KB", "POST"))
        appList.add(AppTrafficItem("Navegador Web", "com.android.chrome", "1.2 MB", "GET"))
        appList.add(AppTrafficItem("Sistema Android", "android", "48 KB", "GET"))

        adapter = AppTrafficAdapter(appList)
        rvAppTraffic.layoutManager = LinearLayoutManager(this)
        rvAppTraffic.adapter = adapter
    }

    private fun startLiveDataSimulation() {
        val random = Random()
        val notificationManager = SecurityNotificationManager(this)

        handler.postDelayed(object : Runnable {
            override fun run() {
                timeX += 1f
                val bytes = random.nextInt(500) + 50
                entries.add(Entry(timeX, bytes.toFloat()))
                if (entries.size > 20) entries.removeAt(0)

                val dataSet = LineDataSet(entries, "Bytes/s").apply {
                    color = Color.parseColor("#00E5FF")
                    valueTextColor = Color.TRANSPARENT
                    lineWidth = 2.5f
                    setDrawCircles(false)
                }
                lineChart.data = LineData(dataSet)
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()

                // Simulación de detección de anomalía
                if (bytes > 450) {
                    notificationManager.sendSecurityAlert(
                        "¡Alerta de Tráfico Anómalo!",
                        "Uso pico detectado: $bytes KB/s enviado en segundo plano."
                    )
                }

                handler.postDelayed(this, 1500)
            }
        }, 1000)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
