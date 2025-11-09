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
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select VPN Server"
        
        prefsManager = PreferencesManager(this)
        
        setupRecyclerView()
        loadServers()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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
                val apiService = RetrofitClient.apiService
                val response = apiService.getServers()
                
                if (response.isSuccessful && response.body() != null) {
                    val serverList = response.body()!!
                    if (serverList.isNotEmpty()) {
                        servers.clear()
                        servers.addAll(serverList)
                        adapter.notifyDataSetChanged()
                        
                        // Test latencies
                        testLatencies()
                    } else {
                        // No servers from API, load mock servers
                        loadMockServers()
                    }
                } else {
                    // API error, load mock servers
                    loadMockServers()
                }
            } catch (e: Exception) {
                // Network error, load mock servers
                Toast.makeText(this@ServerSelectionActivity, 
                    "Using demo servers (API unavailable)", Toast.LENGTH_LONG).show()
                loadMockServers()
            }
        }
    }
    
    private fun loadMockServers() {
        servers.clear()
        servers.addAll(listOf(
            Server(
                id = "us-east-1",
                name = "United States (East)",
                location = "New York, USA",
                ipAddress = "45.79.123.45",
                status = "active",
                publicKey = null,
                createdAt = null
            ),
            Server(
                id = "us-west-1",
                name = "United States (West)",
                location = "Los Angeles, USA",
                ipAddress = "45.79.234.56",
                status = "active",
                publicKey = null,
                createdAt = null
            ),
            Server(
                id = "eu-central-1",
                name = "Germany",
                location = "Frankfurt, Germany",
                ipAddress = "139.162.123.78",
                status = "active",
                publicKey = null,
                createdAt = null
            ),
            Server(
                id = "eu-west-1",
                name = "United Kingdom",
                location = "London, UK",
                ipAddress = "139.162.234.89",
                status = "active",
                publicKey = null,
                createdAt = null
            ),
            Server(
                id = "asia-east-1",
                name = "Singapore",
                location = "Singapore",
                ipAddress = "139.162.45.90",
                status = "active",
                publicKey = null,
                createdAt = null
            ),
            Server(
                id = "asia-northeast-1",
                name = "Japan",
                location = "Tokyo, Japan",
                ipAddress = "139.162.56.101",
                status = "active",
                publicKey = null,
                createdAt = null
            )
        ))
        adapter.notifyDataSetChanged()
        testLatencies()
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
