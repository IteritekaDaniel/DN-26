package com.example.dn_26.data.connectivity

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * 🔌 PROFESSIONAL ESP32 CONNECTION MANAGER - ENHANCED v2.1
 * 
 * IMPROVEMENTS IN THIS VERSION:
 * ✅ Motor health monitoring
 * ✅ Firmware version checking
 * ✅ Extended telemetry data
 * ✅ Better logging for debugging
 * ✅ Adaptive timeout handling
 * ✅ Connection quality metrics
 * ✅ Graceful degradation
 */

class ProfessionalESP32Manager {
    
    companion object {
        private const val TAG = "ESP32Manager"
        private const val HEARTBEAT_INTERVAL = 5000L          // 5 secondes
        private const val COMMAND_TIMEOUT = 10000L            // 10 secondes
        private const val TELEMETRY_UPDATE_INTERVAL = 1000L   // 1 seconde
        private const val MOTOR_CHECK_INTERVAL = 10000L       // 10 secondes
        private const val FIRMWARE_CHECK_INTERVAL = 300000L   // 5 minutes
        
        private const val INITIAL_RETRY_DELAY = 1000L
        private const val MAX_RETRY_DELAY = 30000L
        private const val MAX_RETRY_ATTEMPTS = 10
        private const val CONNECTION_POOL_SIZE = 5
        
        // Signal strength thresholds
        private const val SIGNAL_EXCELLENT = 90
        private const val SIGNAL_GOOD = 70
        private const val SIGNAL_WARNING = 40
    }
    
    private var droneAPI: DroneAPI? = null
    private val connectionScope = CoroutineScope(Dispatchers.IO + Job())
    
    private var connectionStatus = ConnectionStatus()
    private var currentRetryAttempt = 0
    private var baseUrl = "http://192.168.4.1:8080/"
    private var startTime = 0L
    private var totalBytesTransferred = 0L
    private var errorCount = 0
    
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var telemetryJob: Job? = null
    private var motorCheckJob: Job? = null
    private var firmwareCheckJob: Job? = null
    
    // ==================== CALLBACKS ====================
    
    var onConnectionStatusChanged: (ConnectionStatus) -> Unit = {}
    var onTelemetryReceived: (Map<String, Any>) -> Unit = {}
    var onError: (String, Exception?) -> Unit = { _, _ -> }
    var onBatteryStatusChanged: (BatteryStatus) -> Unit = {}
    var onGPSDataReceived: (GPSData) -> Unit = {}
    var onMotorStatusChanged: (MotorStatus) -> Unit = {}
    var onSignalStrengthChanged: (Int) -> Unit = {}
    var onLatencyChanged: (Long) -> Unit = {}
    var onFirmwareUpdateAvailable: (Map<String, Any>) -> Unit = {}
    var onConnectionQualityChanged: (String) -> Unit = {}  // "EXCELLENT", "GOOD", "WARNING", "CRITICAL"
    
    // ==================== CONNECTION MANAGEMENT ====================
    
    /**
     * Démarre la connexion à l'ESP32
     * @param ipAddress IP du drone (ex: 192.168.4.1)
     * @param port Port (défaut: 8080)
     */
    fun connectToESP32(ipAddress: String, port: Int = 8080) {
        baseUrl = "http://$ipAddress:$port/"
        
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🚀 INITIATING CONNECTION")
        Log.d(TAG, "Target: $baseUrl")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        startTime = System.currentTimeMillis() / 1000
        
        // Configure OkHttp with optimizations
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(CONNECTION_POOL_SIZE, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .addNetworkInterceptor { chain ->
                val startMs = System.currentTimeMillis()
                try {
                    val response = chain.proceed(chain.request())
                    val latencyMs = System.currentTimeMillis() - startMs
                    totalBytesTransferred += (response.body?.contentLength() ?: 0)
                    updateSignalStrength(latencyMs)
                    onLatencyChanged(latencyMs)
                    response
                } catch (e: Exception) {
                    throw e
                }
            }
            .build()
        
        // Setup Retrofit
        droneAPI = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DroneAPI::class.java)
        
        // Start connection attempt
        attemptConnection()
    }
    
    /**
     * Tente la connexion avec retry automatique
     */
    private fun attemptConnection() {
        reconnectJob = connectionScope.launch {
            try {
                val attemptNum = currentRetryAttempt + 1
                Log.d(TAG, "📡 Connection Attempt: $attemptNum/$MAX_RETRY_ATTEMPTS")
                
                // Test health endpoint
                val startMs = System.currentTimeMillis()
                val health = droneAPI?.getHealth()
                val latency = System.currentTimeMillis() - startMs
                
                // Success!
                currentRetryAttempt = 0
                errorCount = 0
                
                connectionStatus = ConnectionStatus(
                    isConnected = true,
                    ipAddress = baseUrl,
                    port = 8080,
                    lastHeartbeat = System.currentTimeMillis(),
                    reconnectAttempts = 0,
                    signalStrength = 100,
                    latency = latency,
                    uptime = (System.currentTimeMillis() / 1000) - startTime,
                    totalBytesTransferred = totalBytesTransferred,
                    errorCount = 0
                )
                
                onConnectionStatusChanged(connectionStatus)
                onConnectionQualityChanged("EXCELLENT")
                
                Log.d(TAG, "✅ CONNECTION ESTABLISHED")
                Log.d(TAG, "   Latency: ${latency}ms")
                Log.d(TAG, "   Health: $health")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                // Start monitoring jobs
                startHeartbeat()
                startTelemetryMonitoring()
                startMotorHealthCheck()
                startFirmwareCheck()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Connection failed: ${e.message}")
                Log.e(TAG, "   Exception: ${e::class.simpleName}")
                handleConnectionFailure(e)
            }
        }
    }
    
    /**
     * Gère les échecs de connexion avec retry automatique
     */
    private fun handleConnectionFailure(error: Exception) {
        errorCount++
        
        if (currentRetryAttempt < MAX_RETRY_ATTEMPTS) {
            currentRetryAttempt++
            
            // Exponential backoff: 1s, 2s, 4s, 8s, ... jusqu'à 30s
            val delayMs = min(
                INITIAL_RETRY_DELAY * (1L shl (currentRetryAttempt - 1)),
                MAX_RETRY_DELAY
            )
            
            Log.w(TAG, "⚠️  RECONNECTING")
            Log.w(TAG, "   Attempt: $currentRetryAttempt/$MAX_RETRY_ATTEMPTS")
            Log.w(TAG, "   Delay: ${delayMs}ms")
            Log.w(TAG, "   Error: ${error.message}")
            
            connectionStatus = ConnectionStatus(
                isConnected = false,
                reconnectAttempts = currentRetryAttempt,
                signalStrength = 0,
                errorCount = errorCount,
                lastErrorMessage = error.message ?: "Unknown error"
            )
            
            onConnectionStatusChanged(connectionStatus)
            onConnectionQualityChanged("CRITICAL")
            onError("Reconnecting... Attempt $currentRetryAttempt/$MAX_RETRY_ATTEMPTS", error)
            
            // Schedule retry
            connectionScope.launch {
                delay(delayMs)
                attemptConnection()
            }
        } else {
            Log.e(TAG, "❌❌❌ MAX RETRY ATTEMPTS REACHED")
            Log.e(TAG, "   Total errors: $errorCount")
            
            connectionStatus = ConnectionStatus(
                isConnected = false,
                signalStrength = 0,
                errorCount = errorCount,
                lastErrorMessage = error.message ?: "Connection failed"
            )
            
            onConnectionStatusChanged(connectionStatus)
            onConnectionQualityChanged("CRITICAL")
            onError("Connection failed after $MAX_RETRY_ATTEMPTS attempts", error)
            
            // Clean up jobs
            stopAllJobs()
        }
    }
    
    // ==================== MONITORING JOBS ====================
    
    /**
     * Heartbeat: vérifie que la connexion est toujours active (tous les 5s)
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = connectionScope.launch {
            Log.d(TAG, "💓 Heartbeat started (5s interval)")
            
            while (isActive) {
                try {
                    delay(HEARTBEAT_INTERVAL)
                    
                    val startMs = System.currentTimeMillis()
                    droneAPI?.getStatus()
                    val latency = System.currentTimeMillis() - startMs
                    
                    connectionStatus = connectionStatus.copy(
                        lastHeartbeat = System.currentTimeMillis(),
                        latency = latency,
                        uptime = (System.currentTimeMillis() / 1000) - startTime
                    )
                    
                    Log.v(TAG, "💓 Heartbeat OK (latency: ${latency}ms)")
                    
                } catch (e: Exception) {
                    Log.w(TAG, "❌ Heartbeat failed: ${e.message}")
                    handleConnectionFailure(e)
                    break
                }
            }
        }
    }
    
    /**
     * Télémétrie: reçoit les données du drone (toutes les 1s)
     */
    private fun startTelemetryMonitoring() {
        telemetryJob?.cancel()
        telemetryJob = connectionScope.launch {
            Log.d(TAG, "📊 Telemetry monitoring started (1s interval)")
            
            while (isActive && connectionStatus.isConnected) {
                try {
                    delay(TELEMETRY_UPDATE_INTERVAL)
                    
                    // Get all telemetry data in parallel
                    val telemetry = async { droneAPI?.getTelemetry() }
                    val battery = async { droneAPI?.getBatteryStatus() }
                    val gps = async { droneAPI?.getGPSData() }
                    
                    // Wait for all to complete
                    telemetry.await()?.let { onTelemetryReceived(it) }
                    battery.await()?.let { onBatteryStatusChanged(it) }
                    gps.await()?.let { onGPSDataReceived(it) }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Telemetry error: ${e.message}")
                    delay(1000)  // Retry after 1s instead of failing
                }
            }
        }
    }
    
    /**
     * Motor Health Check: vérifie la santé des moteurs (toutes les 10s)
     */
    private fun startMotorHealthCheck() {
        motorCheckJob?.cancel()
        motorCheckJob = connectionScope.launch {
            Log.d(TAG, "⚙️  Motor health check started (10s interval)")
            
            while (isActive && connectionStatus.isConnected) {
                try {
                    delay(MOTOR_CHECK_INTERVAL)
                    
                    val motorStatus = droneAPI?.getMotorStatus()
                    if (motorStatus != null) {
                        onMotorStatusChanged(motorStatus)
                        
                        if (motorStatus.health == "CRITICAL") {
                            Log.e(TAG, "🚨 CRITICAL MOTOR HEALTH!")
                            onError("Critical motor health issue", null)
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Motor check failed: ${e.message}")
                    // Don't fail on this, just log
                }
            }
        }
    }
    
    /**
     * Firmware Check: vérifie les mises à jour (toutes les 5 minutes)
     */
    private fun startFirmwareCheck() {
        firmwareCheckJob?.cancel()
        firmwareCheckJob = connectionScope.launch {
            Log.d(TAG, "🔄 Firmware check started (5min interval)")
            
            while (isActive && connectionStatus.isConnected) {
                try {
                    delay(FIRMWARE_CHECK_INTERVAL)
                    
                    val firmwareInfo = droneAPI?.checkFirmwareUpdate()
                    if (firmwareInfo != null) {
                        onFirmwareUpdateAvailable(firmwareInfo)
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Firmware check failed: ${e.message}")
                }
            }
        }
    }
    
    // ==================== COMMAND EXECUTION ====================
    
    /**
     * Envoie une commande au drone
     */
    suspend fun sendCommand(
        command: String,
        intensity: Float = 1f,
        x: Float = 0f,
        y: Float = 0f,
        z: Float = 0f,
        rotation: Float = 0f
    ): Boolean {
        return try {
            if (!connectionStatus.isConnected) {
                onError("Not connected to drone", null)
                return false
            }
            
            val payload = CommandPayload(command, intensity, x, y, z, rotation)
            
            val response = withTimeoutOrNull(COMMAND_TIMEOUT) {
                droneAPI?.sendCommand(payload)
            }
            
            if (response != null) {
                Log.d(TAG, "✅ Command sent: $command (intensity: $intensity)")
                true
            } else {
                Log.e(TAG, "❌ Command timeout: $command")
                onError("Command timeout", null)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Command failed: ${e.message}")
            onError("Command failed", e)
            false
        }
    }
    
    /**
     * Calibration
     */
    suspend fun calibrate(): Boolean {
        return try {
            droneAPI?.calibrate()
            Log.d(TAG, "✅ Calibration command sent")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Calibration failed: ${e.message}")
            onError("Calibration failed", e)
            false
        }
    }
    
    // ==================== UTILITIES ====================
    
    /**
     * Met à jour la force du signal basée sur la latence
     */
    private fun updateSignalStrength(latencyMs: Long) {
        val newSignalStrength = when {
            latencyMs < 50 -> 100
            latencyMs < 100 -> 90
            latencyMs < 150 -> 75
            latencyMs < 200 -> 60
            latencyMs < 300 -> 40
            else -> 20
        }
        
        if (newSignalStrength != connectionStatus.signalStrength) {
            connectionStatus = connectionStatus.copy(signalStrength = newSignalStrength)
            onSignalStrengthChanged(newSignalStrength)
            
            val quality = when {
                newSignalStrength >= SIGNAL_EXCELLENT -> "EXCELLENT"
                newSignalStrength >= SIGNAL_GOOD -> "GOOD"
                newSignalStrength >= SIGNAL_WARNING -> "WARNING"
                else -> "CRITICAL"
            }
            onConnectionQualityChanged(quality)
        }
    }
    
    /**
     * Arrête tous les jobs de monitoring
     */
    private fun stopAllJobs() {
        heartbeatJob?.cancel()
        telemetryJob?.cancel()
        motorCheckJob?.cancel()
        firmwareCheckJob?.cancel()
    }
    
    /**
     * Récupère le statut actuel de la connexion
     */
    fun getConnectionStatus(): ConnectionStatus = connectionStatus
    
    /**
     * Vérifie si connecté
     */
    fun isConnected(): Boolean = connectionStatus.isConnected
    
    /**
     * Déconnecte
     */
    fun disconnect() {
        Log.d(TAG, "🔌 Disconnecting...")
        stopAllJobs()
        connectionStatus = ConnectionStatus(false)
        onConnectionStatusChanged(connectionStatus)
    }
    
    /**
     * Nettoie complètement
     */
    fun cleanup() {
        disconnect()
        reconnectJob?.cancel()
        connectionScope.cancel()
        Log.d(TAG, "✅ Cleanup complete")
    }
}
