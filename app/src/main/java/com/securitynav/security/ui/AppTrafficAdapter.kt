package com.securitynav.security.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.securitynav.security.R
import com.securitynav.security.data.AppTrafficItem

class AppTrafficAdapter(private val items: List<AppTrafficItem>) :
    RecyclerView.Adapter<AppTrafficAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAppName: TextView = view.findViewById(R.id.tvAppName)
        val tvPackageName: TextView = view.findViewById(R.id.tvPackageName)
        val tvBytes: TextView = view.findViewById(R.id.tvBytesTransferred)
        val tvVerb: TextView = view.findViewById(R.id.tvHttpVerb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_traffic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvAppName.text = item.appName
        holder.tvPackageName.text = item.packageName
        holder.tvBytes.text = item.bytesText
        holder.tvVerb.text = item.httpVerb
    }

    override fun getItemCount(): Int = items.size
}
