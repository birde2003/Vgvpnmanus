package com.veilguard.vpn.ui.servers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val latencyText: TextView = view.findViewById(R.id.latencyText)
        val latencyIndicator: ImageView = view.findViewById(R.id.latencyIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        val latency = latencies[server.id]

        holder.serverName.text = server.name
        holder.serverLocation.text = server.location

        if (latency != null && latency > 0) {
            holder.latencyText.text = "${latency}ms"
            
            // Set indicator color based on latency
            val color = when {
                latency < 50 -> Color.GREEN
                latency < 150 -> Color.YELLOW
                else -> Color.RED
            }
            holder.latencyIndicator.setColorFilter(color)
        } else {
            holder.latencyText.text = "Testing..."
            holder.latencyIndicator.setColorFilter(Color.GRAY)
        }

        holder.itemView.setOnClickListener {
            onServerClick(server)
        }
    }

    override fun getItemCount() = servers.size
}
