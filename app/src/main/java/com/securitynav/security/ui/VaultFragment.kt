package com.securitynav.security.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.securitynav.security.R
import com.securitynav.security.db.SQLCipherHelper

class VaultFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vault, container, false)
        
        val btnUnlock = view.findViewById<Button>(R.id.btnUnlockVault)
        btnUnlock?.setOnClickListener {
            try {
                val dbHelper = SQLCipherHelper(requireContext())
                val db = dbHelper.writableDatabase
                // Perform a simple query to verify it's open
                val cursor = db.rawQuery("SELECT count(*) FROM security_events", null)
                cursor.moveToFirst()
                val count = cursor.getInt(0)
                cursor.close()
                Toast.makeText(requireContext(), "Vault Unlocked. Events: $count", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error accessing Vault: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        return view
    }
}
