package com.example.wifiscanner.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.wifiscanner.R
import com.example.wifiscanner.model.WiFiPoint
import java.text.SimpleDateFormat
import java.util.*

class WiFiEditDialogFragment : DialogFragment() {
    private lateinit var ssidEditText: EditText
    private lateinit var bssidEditText: EditText
    private lateinit var levelEditText: EditText
    private lateinit var latitudeEditText: EditText
    private lateinit var longitudeEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var timestampEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var wifiPoint: WiFiPoint? = null
    private var onSaveCallback: ((WiFiPoint) -> Unit)? = null

    companion object {
        private const val ARG_WIFI_POINT = "wifi_point"
        
        fun newInstance(wifiPoint: WiFiPoint, onSave: (WiFiPoint) -> Unit): WiFiEditDialogFragment {
            val fragment = WiFiEditDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_WIFI_POINT, wifiPoint)
            fragment.arguments = args
            fragment.onSaveCallback = onSave
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_edit, null)
        initViews(dialogView)
        
        wifiPoint = arguments?.getSerializable(ARG_WIFI_POINT) as? WiFiPoint
        
        wifiPoint?.let { point ->
            populateFields(point)
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        
        return dialog
    }

    private fun initViews(view: View) {
        ssidEditText = view.findViewById(R.id.ssid_edit)
        bssidEditText = view.findViewById(R.id.bssid_edit)
        levelEditText = view.findViewById(R.id.level_edit)
        latitudeEditText = view.findViewById(R.id.latitude_edit)
        longitudeEditText = view.findViewById(R.id.longitude_edit)
        descriptionEditText = view.findViewById(R.id.description_edit)
        timestampEditText = view.findViewById(R.id.timestamp_edit)
        saveButton = view.findViewById(R.id.save_button)
        cancelButton = view.findViewById(R.id.cancel_button)

        saveButton.setOnClickListener {
            saveWiFiPoint()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun populateFields(wifiPoint: WiFiPoint) {
        ssidEditText.setText(wifiPoint.ssid)
        bssidEditText.setText(wifiPoint.bssid)
        levelEditText.setText(wifiPoint.level.toString())
        latitudeEditText.setText(wifiPoint.latitude.toString())
        longitudeEditText.setText(wifiPoint.longitude.toString())
        descriptionEditText.setText(wifiPoint.description)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        timestampEditText.setText(dateFormat.format(Date(wifiPoint.timestamp)))
    }

    private fun saveWiFiPoint() {
        val ssid = ssidEditText.text.toString()
        val bssid = bssidEditText.text.toString()
        val level = levelEditText.text.toString().toIntOrNull() ?: 0
        val latitude = latitudeEditText.text.toString().toDoubleOrNull() ?: 0.0
        val longitude = longitudeEditText.text.toString().toDoubleOrNull() ?: 0.0
        val description = descriptionEditText.text.toString()

        val updatedPoint = wifiPoint?.copy(
            ssid = ssid,
            bssid = bssid,
            level = level,
            latitude = latitude,
            longitude = longitude,
            description = description
        )

        updatedPoint?.let { point ->
            onSaveCallback?.invoke(point)
        }
        dismiss()
    }
}