package com.veilguard.vpn.ui.servers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.veilguard.vpn.R
import com.veilguard.vpn.data.model.Server

class ServerAdapter(
    private val servers: List<Server>,
    private val latencies: Map<String, Long>,
    private val onServerClick: (Server) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {
    
    class ServerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serverName: TextView = view.findViewById(R.id.serverName)
        val serverLocation: TextView = view.findViewById(R.id.serverLocation)
        val serverLatency: TextView = view.findViewById(R.id.serverLatency)
        val latencyIndicator: View = view.findViewById(R.id.latencyIndicator)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        val latency = latencies[server.id] ?: -1L
        
        holder.serverName.text = server.name
        holder.serverLocation.text = server.location
        
        when {
            latency < 0 -> {
                holder.serverLatency.text = "Offline"
                holder.latencyIndicator.setBackgroundColor(Color.RED)
            }
            latency < 50 -> {
                holder.serverLatency.text = "${latency}ms"
                holder.latencyIndicator.setBackgroundColor(Color.GREEN)
            }
            latency < 100 -> {
                holder.serverLatency.text = "${latency}ms"
                holder.latencyIndicator.setBackgroundColor(Color.YELLOW)
            }
            else -> {
                holder.serverLatency.text = "${latency}ms"
                holder.latencyIndicator.setBackgroundColor(Color.rgb(255, 165, 0))
            }
        }
        
        holder.itemView.setOnClickListener {
            onServerClick(server)
        }
    }
    
    override fun getItemCount() = servers.size
}
