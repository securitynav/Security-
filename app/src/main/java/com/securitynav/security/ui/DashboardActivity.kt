package com.securitynav.security.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R
import com.securitynav.security.data.database.SecurityDatabase

class DashboardActivity : AppCompatActivity() {

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val tvLogsSummary = findViewById<TextView>(R.id.tvLogsSummary)
        val tvLogsDetail = findViewById<TextView>(R.id.tvLogsDetail)

        try {
            val dbHelper = SecurityDatabase(this)
            val db = dbHelper.getReadableEncryptedDatabase()
            val cursor = db.rawQuery("SELECT timestamp, event_type, details FROM security_logs ORDER BY id DESC LIMIT 100", null)

            val builder = StringBuilder()
            var totalEvents = 0
            var getCount = 0
            var postCount = 0

            while (cursor.moveToNext()) {
                totalEvents++
                val time = cursor.getLong(0)
                val type = cursor.getString(1)
                val details = cursor.getString(2)

                if (details.contains("[GET]")) getCount++
                if (details.contains("[POST]")) postCount++

                builder.append("[$time] $type:\n$details\n-------------------------------\n")
            }
            cursor.close()
            db.close()

            tvLogsSummary.text = "Métricas: $totalEvents Eventos | HTTP GET: $getCount | HTTP POST: $postCount"
            tvLogsDetail.text = if (builder.isNotEmpty()) builder.toString() else "Esperando captura de tráfico en tiempo real..."

        } catch (e: Exception) {
            tvLogsSummary.text = "Error al leer el registro de inspección de tráfico"
            tvLogsDetail.text = e.localizedMessage
        }
    }
}
