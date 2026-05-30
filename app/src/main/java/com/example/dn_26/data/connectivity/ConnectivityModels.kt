package com.example.dn_26.data.connectivity

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 🛰️ CONNECTIVITY MODELS v2.1 - ENHANCED
 */

interface DroneAPI {
    @GET("api/health")
    suspend fun getHealth(): Map<String, Any>
    
    @GET("api/telemetry")
    suspend fun getTelemetry(): Map<String, Any>
    
    @POST("api/command")
    suspend fun sendCommand(@Body command: CommandPayload): Map<String, Any>
    
    @GET("api/status")
    suspend fun getStatus(): Map<String, Any>
    
    @GET("api/battery")
    suspend fun getBatteryStatus(): BatteryStatus
    
    @GET("api/gps")
    suspend fun getGPSData(): GPSData
    
    @GET("api/motors")
    suspend fun getMotorStatus(): MotorStatus
    
    @POST("api/calibrate")
    suspend fun calibrate(): Map<String, Any>

    @POST("api/firmware/check")
    suspend fun checkFirmwareUpdate(): Map<String, Any>
}

data class CommandPayload(
    val command: String,
    val intensity: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val rotation: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class BatteryStatus(
    val percentage: Float,
    val voltage: Float,
    val current: Float,
    val temperature: Float,
    val health: String,
    val cycles: Int = 0,
    val capacity: Float = 0f,
    val estimatedTimeRemaining: Long = 0L // in seconds
)

data class GPSData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val satellites: Int,
    val hdop: Float,
    val speed: Float = 0f,
    val heading: Float = 0f
)

data class MotorStatus(
    val motor1: Int,        // RPM
    val motor2: Int,        // RPM
    val motor3: Int,        // RPM
    val motor4: Int,        // RPM
    val temperature: Float,
    val health: String,     // "GOOD", "WARNING", "CRITICAL"
    val avgCurrent: Float = 0f,
    val avgVoltage: Float = 0f
)

data class ConnectionStatus(
    val isConnected: Boolean = false,
    val ipAddress: String = "",
    val port: Int = 8080,
    val lastHeartbeat: Long = 0,
    val reconnectAttempts: Int = 0,
    val signalStrength: Int = 0,        // 0-100%
    val latency: Long = 0,              // milliseconds
    val uptime: Long = 0,               // seconds
    val totalBytesTransferred: Long = 0,
    val errorCount: Int = 0,
    val lastErrorMessage: String = ""
)
