package com.example.wifiscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wifiscanner.R
import com.example.wifiscanner.db.WiFiDatabase
import com.example.wifiscanner.model.WiFiPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WiFiListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WiFiListAdapter
    private lateinit var database: WiFiDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifi_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_view)
        database = WiFiDatabase.getDatabase(requireContext())
        
        adapter = WiFiListAdapter { wifiPoint ->
            // Handle item click - could open edit dialog
            openEditDialog(wifiPoint)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        loadWiFiPoints()
    }

    private fun loadWiFiPoints() {
        CoroutineScope(Dispatchers.IO).launch {
            val wifiPoints = database.wifiPointDao().getAll()
            requireActivity().runOnUiThread {
                adapter.submitList(wifiPoints)
            }
        }
    }

    private fun openEditDialog(wifiPoint: WiFiPoint) {
        val dialog = WiFiEditDialogFragment.newInstance(wifiPoint) { updatedPoint ->
            // Update the database
            CoroutineScope(Dispatchers.IO).launch {
                database.wifiPointDao().update(updatedPoint)
                loadWiFiPoints() // Refresh the list
            }
        }
        dialog.show(parentFragmentManager, "WiFiEditDialog")
    }
}