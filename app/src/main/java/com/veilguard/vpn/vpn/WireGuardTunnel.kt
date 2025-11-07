package com.veilguard.vpn.vpn

import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.Selector
import java.nio.channels.SelectionKey

/**
 * WireGuard tunnel implementation for VPN connectivity
 * This handles the actual packet routing through the VPN tunnel
 */
class WireGuardTunnel(
    private val vpnInterface: ParcelFileDescriptor,
    private val serverAddress: String,
    private val serverPort: Int = 51820
) {
    private val TAG = "WireGuardTunnel"
    private var isRunning = false
    private var tunnelChannel: DatagramChannel? = null
    private var selector: Selector? = null
    
    fun start(protectSocket: (DatagramChannel) -> Boolean) {
        isRunning = true
        
        try {
            // Create UDP channel for VPN tunnel
            tunnelChannel = DatagramChannel.open()
            tunnelChannel?.configureBlocking(false)
            tunnelChannel?.connect(InetSocketAddress(serverAddress, serverPort))
            
            // Protect socket from being routed through VPN (prevents loop)
            if (!protectSocket(tunnelChannel!!)) {
                Log.e(TAG, "Failed to protect socket")
                stop()
                return
            }
            
            // Create selector for non-blocking I/O
            selector = Selector.open()
            tunnelChannel?.register(selector, SelectionKey.OP_READ)
            
            // Start tunnel loop
            runTunnel()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tunnel", e)
            stop()
        }
    }
    
    fun stop() {
        isRunning = false
        
        try {
            selector?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing selector", e)
        }
        
        try {
            tunnelChannel?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing tunnel channel", e)
        }
    }
    
    private fun runTunnel() {
        val vpnInput = FileInputStream(vpnInterface.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface.fileDescriptor)
        
        val deviceToNetwork = ByteBuffer.allocate(32767)
        val networkToDevice = ByteBuffer.allocate(32767)
        
        try {
            while (isRunning) {
                // Read from VPN interface (packets from device)
                deviceToNetwork.clear()
                val length = vpnInput.channel.read(deviceToNetwork)
                
                if (length > 0) {
                    deviceToNetwork.flip()
                    
                    // Send packet to VPN server
                    try {
                        tunnelChannel?.write(deviceToNetwork)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending to tunnel", e)
                    }
                }
                
                // Check for packets from VPN server
                selector?.selectNow()
                val keys = selector?.selectedKeys()
                
                if (keys != null && keys.isNotEmpty()) {
                    for (key in keys) {
                        if (key.isReadable) {
                            networkToDevice.clear()
                            val received = tunnelChannel?.read(networkToDevice) ?: 0
                            
                            if (received > 0) {
                                networkToDevice.flip()
                                
                                // Write packet to VPN interface (back to device)
                                vpnOutput.channel.write(networkToDevice)
                            }
                        }
                    }
                    keys.clear()
                }
                
                // Small sleep to prevent busy waiting
                Thread.sleep(1)
            }
        } catch (e: InterruptedException) {
            Log.i(TAG, "Tunnel interrupted")
        } catch (e: Exception) {
            Log.e(TAG, "Error in tunnel loop", e)
        } finally {
            try {
                vpnInput.close()
                vpnOutput.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing streams", e)
            }
        }
    }
}
