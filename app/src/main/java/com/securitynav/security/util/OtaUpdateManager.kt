package com.securitynav.security.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.securitynav.security.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtaUpdateManager(private val context: Context) {

    fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.securityApiService.checkUpdate()
                if (response.isSuccessful && response.body() != null) {
                    val updateData = response.body()!!
                    if (updateData.hasUpdate) {
                        withContext(Dispatchers.Main) {
                            showUpdateDialog(updateData.versionName, updateData.releaseNotes, updateData.downloadUrl, updateData.forceUpdate)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors for OTA check or log them
            }
        }
    }

    private fun showUpdateDialog(version: String, notes: String, url: String, force: Boolean) {
        if (context !is Activity || context.isFinishing) return

        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("New Update Available (\${version})")
            .setMessage(notes)
            .setPositiveButton("Download") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open download link", Toast.LENGTH_SHORT).show()
                }
            }
            
        if (!force) {
            builder.setNegativeButton("Later", null)
        }
        
        val dialog = builder.create()
        dialog.setCancelable(!force)
        dialog.show()
    }
}
