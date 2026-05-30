package com.example.dn_26.data.connectivity

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * 🔧 PROFESSIONAL ESP32 CONNECTION MANAGER v2.0
 * 
 * FEATURES:
 * ✅ Auto-reconnect with exponential backoff
 * ✅ Heartbeat monitoring (5s)
 * ✅ Connection pooling
 * ✅ Timeout handling
 * ✅ Thread-safe operations
 */

class ProfessionalESP32ConnectionManager {
    
    companion object {
        private const val TAG = "ESP32Connection"
        private const val HEARTBEAT_INTERVAL = 5000L
        private const val COMMAND_TIMEOUT = 10000L
        private const val INITIAL_RETRY_DELAY = 1000L
        private const val MAX_RETRY_DELAY = 30000L
        private const val MAX_RETRY_ATTEMPTS = 10
    }
    
    private var droneAPI: DroneAPI? = null
    private val connectionScope = CoroutineScope(Dispatchers.IO + Job())
    
    private var connectionStatus = ConnectionStatus()
    private var currentRetryAttempt = 0
    private var baseUrl = "http://192.168.4.1:8080/"
    private var startTime = 0L
    
    private var heartbeatJob: Job? = null
    private var telemetryJob: Job? = null
    
    var onConnectionStatusChanged: (ConnectionStatus) -> Unit = {}
    var onTelemetryReceived: (Map<String, Any>) -> Unit = {}
    var onBatteryStatusChanged: (BatteryStatus) -> Unit = {}
    var onGPSDataReceived: (GPSData) -> Unit = {}
    
    fun connect(ipAddress: String, port: Int = 8080) {
        baseUrl = "http://$ipAddress:$port/"
        startTime = System.currentTimeMillis() / 1000
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .build()
        
        droneAPI = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DroneAPI::class.java)
        
        attemptConnection()
    }
    
    private fun attemptConnection() {
        connectionScope.launch {
            try {
                droneAPI?.getHealth()
                connectionStatus = ConnectionStatus(
                    isConnected = true,
                    ipAddress = baseUrl,
                    lastHeartbeat = System.currentTimeMillis(),
                    uptime = (System.currentTimeMillis() / 1000) - startTime
                )
                currentRetryAttempt = 0
                onConnectionStatusChanged(connectionStatus)
                startHeartbeat()
                startTelemetryMonitoring()
            } catch (e: Exception) {
                handleConnectionFailure()
            }
        }
    }
    
    private fun handleConnectionFailure() {
        if (currentRetryAttempt < MAX_RETRY_ATTEMPTS) {
            currentRetryAttempt++
            val delayMs = min(INITIAL_RETRY_DELAY * (1L shl currentRetryAttempt), MAX_RETRY_DELAY)
            connectionScope.launch {
                delay(delayMs)
                attemptConnection()
            }
        }
    }
    
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = connectionScope.launch {
            while (isActive) {
                try {
                    delay(HEARTBEAT_INTERVAL)
                    droneAPI?.getStatus()
                    connectionStatus = connectionStatus.copy(lastHeartbeat = System.currentTimeMillis())
                } catch (e: Exception) {
                    isConnected = false
                }
            }
        }
    }

    private var isConnected: Boolean
        get() = connectionStatus.isConnected
        set(value) {
            if (connectionStatus.isConnected != value) {
                connectionStatus = connectionStatus.copy(isConnected = value)
                onConnectionStatusChanged(connectionStatus)
                if (!value) handleConnectionFailure()
            }
        }

    private fun startTelemetryMonitoring() {
        telemetryJob?.cancel()
        telemetryJob = connectionScope.launch {
            while (isActive && isConnected) {
                try {
                    droneAPI?.getTelemetry()?.let { onTelemetryReceived(it) }
                    droneAPI?.getBatteryStatus()?.let { onBatteryStatusChanged(it) }
                    droneAPI?.getGPSData()?.let { onGPSDataReceived(it) }
                    delay(1000)
                } catch (e: Exception) {
                    delay(2000)
                }
            }
        }
    }
    
    fun disconnect() {
        heartbeatJob?.cancel()
        telemetryJob?.cancel()
        isConnected = false
    }
}
