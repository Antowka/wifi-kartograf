package com.example.wifiscanner.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wifiscanner.model.WiFiPoint

@Database(
    entities = [WiFiPoint::class],
    version = 1,
    exportSchema = false
)
abstract class WiFiDatabase : RoomDatabase() {
    abstract fun wifiPointDao(): WiFiPointDao

    companion object {
        @Volatile
        private var INSTANCE: WiFiDatabase? = null

        fun getDatabase(context: Context): WiFiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WiFiDatabase::class.java,
                    "wifi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}