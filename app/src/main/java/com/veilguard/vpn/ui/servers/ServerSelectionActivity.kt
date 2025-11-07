package com.veilguard.vpn.ui.servers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.veilguard.vpn.R
import com.veilguard.vpn.api.RetrofitClient
import com.veilguard.vpn.data.local.PreferencesManager
import com.veilguard.vpn.data.model.Server
import com.veilguard.vpn.vpn.ServerLatencyTester
import kotlinx.coroutines.launch

class ServerSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServerAdapter
    private lateinit var prefsManager: PreferencesManager
    private val latencyTester = ServerLatencyTester()
    private val servers = mutableListOf<Server>()
    private val latencies = mutableMapOf<String, Long>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_selection)
        
        prefsManager = PreferencesManager(this)
        
        setupRecyclerView()
        loadServers()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.serversRecyclerView)
        adapter = ServerAdapter(servers, latencies) { server ->
            selectServer(server)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadServers() {
        lifecycleScope.launch {
            try {
                val token = prefsManager.getAuthToken() ?: return@launch
                val apiService = RetrofitClient.getApiService()
                val response = apiService.getServers()
                
                if (response.isSuccessful) {
                    response.body()?.let { serverList ->
                        servers.clear()
                        servers.addAll(serverList)
                        adapter.notifyDataSetChanged()
                        
                        // Test latencies
                        testLatencies()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ServerSelectionActivity, 
                    "Failed to load servers", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun testLatencies() {
        lifecycleScope.launch {
            servers.forEach { server ->
                val latency = latencyTester.testLatency(server)
                latencies[server.id] = latency
                adapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun selectServer(server: Server) {
        prefsManager.setSelectedServer(server.id, server.name, server.ipAddress)
        Toast.makeText(this, "Selected: ${server.name}", Toast.LENGTH_SHORT).show()
        finish()
    }
}
