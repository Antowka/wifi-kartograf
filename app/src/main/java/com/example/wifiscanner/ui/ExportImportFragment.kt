package com.example.wifiscanner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wifiscanner.R
import com.example.wifiscanner.db.WiFiDatabase
import com.example.wifiscanner.model.WiFiPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class ExportImportFragment : Fragment() {
    private lateinit var database: WiFiDatabase
    private val gson = Gson()
    private val exportAllRequestCode = 1001
    private val importRequestCode = 1002

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        database = WiFiDatabase.getDatabase(requireContext())
        
        val exportAllButton = view.findViewById<Button>(R.id.export_all_button)
        val importButton = view.findViewById<Button>(R.id.import_button)
        
        exportAllButton.setOnClickListener {
            exportAllWiFiPoints()
        }
        
        importButton.setOnClickListener {
            openImportFileChooser()
        }
    }

    private fun exportAllWiFiPoints() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val wifiPoints = database.wifiPointDao().getAll()
                val json = gson.toJson(wifiPoints)
                
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "wifi_points_export.json")
                }
                
                startActivityForResult(intent, exportAllRequestCode)
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openImportFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        
        startActivityForResult(intent, importRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == requireActivity().RESULT_OK) {
            data?.data?.let { uri ->
                when (requestCode) {
                    exportAllRequestCode -> {
                        writeExportToFile(uri)
                    }
                    importRequestCode -> {
                        importFromFile(uri)
                    }
                }
            }
        }
    }

    private fun writeExportToFile(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val wifiPoints = database.wifiPointDao().getAll()
                val json = gson.toJson(wifiPoints)
                
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Export successful", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun importFromFile(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader().use { reader ->
                    reader?.readText() ?: ""
                }
                
                val wifiPointsType = object : TypeToken<List<WiFiPoint>>() {}.type
                val importedPoints: List<WiFiPoint> = gson.fromJson(jsonString, wifiPointsType)
                
                // Import points, ignoring duplicates
                importedPoints.forEach { point ->
                    val existingPoint = database.wifiPointDao().getBySsidAndBssid(point.ssid, point.bssid)
                    if (existingPoint == null) {
                        database.wifiPointDao().insert(point)
                    }
                }
                
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Import successful: ${importedPoints.size} points", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}