package com.securitynav.security.ui

import android.os.Bundle
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
            val cursor = db.rawQuery("SELECT timestamp, event_type, details FROM security_logs ORDER BY id DESC LIMIT 50", null)

            val builder = StringBuilder()
            var count = 0

            while (cursor.moveToNext()) {
                count++
                val time = cursor.getLong(0)
                val type = cursor.getString(1)
                val details = cursor.getString(2)
                builder.append("[$time] $type:\n$details\n-------------------------------\n")
            }
            cursor.close()
            db.close()

            tvLogsSummary.text = "Eventos en Almacén Cifrado (Protegido por KeyStore HW): $count"
            tvLogsDetail.text = if (builder.isNotEmpty()) builder.toString() else "No hay eventos anómalos registrados."

        } catch (e: Exception) {
            tvLogsSummary.text = "Error al acceder al almacén cifrado"
            tvLogsDetail.text = e.localizedMessage
        }
    }
}
