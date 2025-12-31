package com.example.wifiscanner.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wifiscanner.R
import com.example.wifiscanner.model.WiFiPoint

class WiFiListAdapter(
    private val onItemClick: (WiFiPoint) -> Unit
) : ListAdapter<WiFiPoint, WiFiListAdapter.WiFiViewHolder>(WiFiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WiFiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi_point, parent, false)
        return WiFiViewHolder(view)
    }

    override fun onBindViewHolder(holder: WiFiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WiFiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ssidTextView: TextView = itemView.findViewById(R.id.ssid_text)
        private val bssidTextView: TextView = itemView.findViewById(R.id.bssid_text)
        private val levelTextView: TextView = itemView.findViewById(R.id.level_text)
        private val coordinatesTextView: TextView = itemView.findViewById(R.id.coordinates_text)

        fun bind(wifiPoint: WiFiPoint) {
            ssidTextView.text = wifiPoint.ssid
            bssidTextView.text = "BSSID: ${wifiPoint.bssid}"
            levelTextView.text = "Level: ${wifiPoint.level} dBm"
            coordinatesTextView.text = "Lat: ${wifiPoint.latitude}, Lng: ${wifiPoint.longitude}"
            
            itemView.setOnClickListener {
                onItemClick(wifiPoint)
            }
        }
    }

    class WiFiDiffCallback : DiffUtil.ItemCallback<WiFiPoint>() {
        override fun areItemsTheSame(oldItem: WiFiPoint, newItem: WiFiPoint): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WiFiPoint, newItem: WiFiPoint): Boolean {
            return oldItem == newItem
        }
    }
}