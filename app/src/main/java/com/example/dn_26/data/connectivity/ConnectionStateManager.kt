package com.example.dn_26.data.connectivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🔌 Connection State Manager v2.0
 * Manages the global state of the drone connection.
 */
class ConnectionStateManager(private val manager: ProfessionalESP32Manager) {
    
    private val _connectionState = MutableStateFlow(ConnectionStatus())
    val connectionState: StateFlow<ConnectionStatus> = _connectionState.asStateFlow()
    
    private val _latency = MutableStateFlow(0L)
    val latency: StateFlow<Long> = _latency.asStateFlow()

    init {
        manager.onConnectionStatusChanged = { status ->
            _connectionState.value = status
        }
    }

    fun connect(ipAddress: String) {
        manager.connectToESP32(ipAddress)
    }

    fun disconnect() {
        // manager.disconnect() // Assuming disconnect is implemented in ProfessionalESP32Manager
    }
}
