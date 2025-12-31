package com.example.wifiscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wifiscanner.R
import com.example.wifiscanner.db.WiFiDatabase
import com.example.wifiscanner.model.WiFiPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var database: WiFiDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = WiFiDatabase.getDatabase(requireContext())
        
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        loadWiFiPoints()
    }

    private fun loadWiFiPoints() {
        CoroutineScope(Dispatchers.IO).launch {
            val wifiPoints = database.wifiPointDao().getAll()
            
            activity?.runOnUiThread {
                wifiPoints.forEach { wifiPoint ->
                    val location = LatLng(wifiPoint.latitude, wifiPoint.longitude)
                    val markerOptions = MarkerOptions()
                        .position(location)
                        .title(wifiPoint.ssid)
                        .snippet("BSSID: ${wifiPoint.bssid}\nLevel: ${wifiPoint.level} dBm")
                    
                    val marker = mMap.addMarker(markerOptions)
                    marker?.tag = wifiPoint
                }
                
                // Move camera to first point if available
                if (wifiPoints.isNotEmpty()) {
                    val firstPoint = wifiPoints[0]
                    val firstLocation = LatLng(firstPoint.latitude, firstPoint.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15f))
                }
            }
        }
    }
}