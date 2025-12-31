package com.example.wifiscanner.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.wifiscanner.R
import com.example.wifiscanner.db.WiFiDatabase
import com.example.wifiscanner.model.WiFiPoint
import com.example.wifiscanner.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WiFiScanningService : Service() {
    private lateinit var wifiManager: WifiManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: WiFiDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var scanJob: Job? = null
    private val scanInterval = 30000L // 30 seconds
    private val locationRequest = LocationRequest.Builder(30000).apply {
        setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
        setMinUpdateDistanceMeters(10f)
        setMinUpdateIntervalMillis(30000)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                scanAndSaveWiFiPoints(location)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = WiFiDatabase.getDatabase(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        startLocationUpdates()
        startScanning()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
        stopLocationUpdates()
    }

    private fun startScanning() {
        scanJob = serviceScope.launch {
            while (true) {
                getCurrentLocationAndScan()
                kotlinx.coroutines.delay(scanInterval)
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
    }

    private fun getCurrentLocationAndScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                scanAndSaveWiFiPoints(it)
            } ?: run {
                // Request location updates if last location is not available
                startLocationUpdates()
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun scanAndSaveWiFiPoints(location: Location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val scanResults = wifiManager.scanResults
        serviceScope.launch {
            scanResults.forEach { scanResult ->
                // Check if this WiFi point already exists in the database
                val existingPoint = database.wifiPointDao().getBySsidAndBssid(scanResult.SSID, scanResult.BSSID)
                
                if (existingPoint == null) {
                    // New WiFi point, save it
                    val wifiPoint = WiFiPoint(
                        ssid = scanResult.SSID,
                        bssid = scanResult.BSSID,
                        level = scanResult.level,
                        timestamp = System.currentTimeMillis(),
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    database.wifiPointDao().insert(wifiPoint)
                } else {
                    // Optional: Update location/level if significantly changed
                    // For now, we'll just ignore duplicates
                }
            }
        }
    }

    private fun createNotification(): NotificationCompat.Builder {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(this, "WIFI_SCANNING_CHANNEL")
            .setContentTitle("WiFi Scanner Service")
            .setContentText("Scanning WiFi networks in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "WIFI_SCANNING_CHANNEL",
                "WiFi Scanning Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service for scanning WiFi networks"
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}