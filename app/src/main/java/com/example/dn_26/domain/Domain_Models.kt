package com.example.dn_26.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Drone(
    val id: String = "DRONE-001",
    val name: String = "DroneX Pro",
    val batteryLevel: Int = 100,
    val isConnected: Boolean = false,
    val isFlying: Boolean = false,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Serializable
data class Telemetry(
    val timestamp: Long = System.currentTimeMillis(),
    val altitude: Double = 0.0,
    val speed: Double = 0.0,
    val temperature: Double = 25.0,
    val batteryVoltage: Double = 12.6,
    val gpsSatellites: Int = 0,
    val gpsSignalStrength: Int = 0,
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val yaw: Double = 0.0,
    val windSpeed: Double = 0.0,
    val humidity: Double = 50.0
)

@Serializable
data class Alert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val severity: AlertSeverity = AlertSeverity.LOW,
    val type: AlertType = AlertType.SYSTEM,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val code: String = ""
)

enum class AlertSeverity {
    LOW, MEDIUM, CRITICAL;
    companion object {
        fun fromValue(value: String): AlertSeverity = try { valueOf(value.uppercase()) } catch (e: Exception) { LOW }
    }
}

enum class AlertType {
    SYSTEM, WEATHER, BATTERY, THERMAL, NAVIGATION, ANOMALY, PREDICTION
}

@Serializable
data class AIAnalysis(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val anomalies: List<Anomaly> = emptyList(),
    val predictions: List<Prediction> = emptyList(),
    val riskScore: Double = 0.0,
    val recommendations: List<String> = emptyList(),
    val analysisTime: Long = 0L
)

@Serializable
data class Anomaly(
    val id: String = "",
    val category: String = "",
    val confidence: Double = 0.0,
    val description: String = "",
    val affectedMetric: String = "",
    val normalValue: Double = 0.0,
    val actualValue: Double = 0.0,
    val severity: AlertSeverity = AlertSeverity.MEDIUM
)

@Serializable
data class Prediction(
    val id: String = "",
    val component: String = "",
    val failureType: String = "",
    val probability: Double = 0.0,
    val estimatedTimeToFailure: Long = 0L,
    val severity: AlertSeverity = AlertSeverity.MEDIUM,
    val suggestedActions: List<String> = emptyList()
)

enum class DroneCommand {
    ARM,
    DISARM,
    TAKEOFF,
    LAND,
    HOVER,
    EMERGENCY_STOP,
    MOVE_FORWARD,
    MOVE_BACKWARD,
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    ROTATE_CLOCKWISE,
    ROTATE_COUNTER_CLOCKWISE,
    CALIBRATE,
    RETURN_HOME,
    START_RECORDING,
    STOP_RECORDING
}

enum class DroneState {
    DISCONNECTED, CONNECTED, INITIALIZING, IDLE, TAKING_OFF, FLYING, LANDING, LANDED, EMERGENCY_STOP, ERROR
}

enum class ConnectionMode {
    SIMULATION, ESP32_WIFI, BLUETOOTH
}

@Serializable
data class ConnectionProfile(
    val mode: ConnectionMode = ConnectionMode.SIMULATION,
    val ipAddress: String = "192.168.4.1",
    val port: Int = 8080,
    val bluetoothAddress: String = "",
    val bluetoothName: String = "ESP32-DRONE"
)

@Serializable
data class DroneMovementParams(
    val speed: Double = 1.0,
    val duration: Long = 1000,
    val direction: String = ""
)
