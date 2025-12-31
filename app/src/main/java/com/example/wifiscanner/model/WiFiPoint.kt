package com.example.wifiscanner.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "wifi_points")
data class WiFiPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ssid: String,
    val bssid: String,
    val level: Int,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val description: String = ""
) : Serializable