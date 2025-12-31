package com.example.wifiscanner.db

import androidx.room.*
import com.example.wifiscanner.model.WiFiPoint

@Dao
interface WiFiPointDao {
    @Query("SELECT * FROM wifi_points")
    suspend fun getAll(): List<WiFiPoint>

    @Query("SELECT * FROM wifi_points WHERE id = :id")
    suspend fun getById(id: Long): WiFiPoint?

    @Insert
    suspend fun insert(wifiPoint: WiFiPoint): Long

    @Update
    suspend fun update(wifiPoint: WiFiPoint)

    @Delete
    suspend fun delete(wifiPoint: WiFiPoint)

    @Query("DELETE FROM wifi_points")
    suspend fun deleteAll()

    @Query("SELECT * FROM wifi_points WHERE ssid = :ssid AND bssid = :bssid LIMIT 1")
    suspend fun getBySsidAndBssid(ssid: String, bssid: String): WiFiPoint?

    @Query("SELECT * FROM wifi_points WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: LongArray): List<WiFiPoint>
}