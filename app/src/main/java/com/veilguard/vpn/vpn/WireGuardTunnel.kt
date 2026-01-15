package com.veilguard.vpn.vpn

import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

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
            tunnelChannel = DatagramChannel.open()
            tunnelChannel?.configureBlocking(false)
            
            if (!protectSocket(tunnelChannel!!)) {
                Log.e(TAG, "Failed to protect socket")
                return
            }
            
            tunnelChannel?.connect(InetSocketAddress(serverAddress, serverPort))
            selector = Selector.open()
            tunnelChannel?.register(selector, SelectionKey.OP_READ)
            
            runTunnel()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tunnel", e)
        } finally {
            stop()
        }
    }
    
    fun stop() {
        isRunning = false
        try {
            selector?.close()
            tunnelChannel?.close()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    private fun runTunnel() {
        val vpnInput = FileInputStream(vpnInterface.fileDescriptor).channel
        val vpnOutput = FileOutputStream(vpnInterface.fileDescriptor).channel
        
        val deviceToNetwork = ByteBuffer.allocate(16384)
        val networkToDevice = ByteBuffer.allocate(16384)
        
        while (isRunning) {
            var idle = true
            
            // 1. Read from Device -> Send to Server
            deviceToNetwork.clear()
            val readFromDevice = vpnInput.read(deviceToNetwork)
            if (readFromDevice > 0) {
                deviceToNetwork.flip()
                tunnelChannel?.write(deviceToNetwork)
                idle = false
            }
            
            // 2. Read from Server -> Write to Device
            if (selector?.selectNow() ?: 0 > 0) {
                val keys = selector?.selectedKeys()
                val iterator = keys?.iterator()
                while (iterator?.hasNext() == true) {
                    val key = iterator.next()
                    iterator.remove()
                    if (key.isReadable) {
                        networkToDevice.clear()
                        val readFromServer = tunnelChannel?.read(networkToDevice) ?: 0
                        if (readFromServer > 0) {
                            networkToDevice.flip()
                            vpnOutput.write(networkToDevice)
                            idle = false
                        }
                    }
                }
            }
            
            if (idle) {
                Thread.sleep(10) // Reduced CPU usage when idle
            }
        }
    }
}
