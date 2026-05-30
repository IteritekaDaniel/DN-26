package com.example.dn_26.data.model

// data/model/ApiModels.kt


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.dn_26.domain.model.*

/**
 * API response models that mirror the structure of backend API responses.
 * These are separate from domain models to allow independent evolution of API contracts.
 */

@Serializable
data class TelemetryResponse(
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("altitude")
    val altitude: Double,
    @SerialName("speed")
    val speed: Double,
    @SerialName("temperature")
    val temperature: Double,
    @SerialName("battery_voltage")
    val batteryVoltage: Double,
    @SerialName("gps_satellites")
    val gpsSatellites: Int,
    @SerialName("gps_signal_strength")
    val gpsSignalStrength: Int,
    @SerialName("pitch")
    val pitch: Double,
    @SerialName("roll")
    val roll: Double,
    @SerialName("yaw")
    val yaw: Double,
    @SerialName("wind_speed")
    val windSpeed: Double,
    @SerialName("humidity")
    val humidity: Double
) {
    /**
     * Maps API response to domain model.
     * This conversion happens in a dedicated mapper class in production.
     */
    fun toDomain(): Telemetry = Telemetry(
        timestamp = timestamp,
        altitude = altitude,
        speed = speed,
        temperature = temperature,
        batteryVoltage = batteryVoltage,
        gpsSatellites = gpsSatellites,
        gpsSignalStrength = gpsSignalStrength,
        pitch = pitch,
        roll = roll,
        yaw = yaw,
        windSpeed = windSpeed,
        humidity = humidity
    )
}

@Serializable
data class DroneStateResponse(
    @SerialName("drone_id")
    val droneId: String,
    @SerialName("name")
    val name: String,
    @SerialName("battery_level")
    val batteryLevel: Int,
    @SerialName("is_connected")
    val isConnected: Boolean,
    @SerialName("is_flying")
    val isFlying: Boolean,
    @SerialName("last_update")
    val lastUpdate: Long
) {
    fun toDomain(): Drone = Drone(
        id = droneId,
        name = name,
        batteryLevel = batteryLevel,
        isConnected = isConnected,
        isFlying = isFlying,
        lastUpdate = lastUpdate
    )
}

@Serializable
data class AlertResponse(
    @SerialName("alert_id")
    val alertId: String,
    @SerialName("title")
    val title: String,
    @SerialName("message")
    val message: String,
    @SerialName("severity")
    val severity: String,
    @SerialName("type")
    val type: String,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("is_read")
    val isRead: Boolean,
    @SerialName("code")
    val code: String
) {
    fun toDomain(): Alert = Alert(
        id = alertId,
        title = title,
        message = message,
        severity = AlertSeverity.fromValue(severity),
        type = AlertType.valueOf(type.uppercase()),
        timestamp = timestamp,
        isRead = isRead,
        code = code
    )
}

/**
 * AI API Request/Response models
 */

@Serializable
data class AIAnalysisRequest(
    @SerialName("drone_id")
    val droneId: String,
    @SerialName("telemetry_data")
    val telemetryData: TelemetryPayload,
    @SerialName("analysis_type")
    val analysisType: String = "comprehensive"
)

@Serializable
data class TelemetryPayload(
    val altitude: Double,
    val speed: Double,
    val temperature: Double,
    @SerialName("battery_voltage")
    val batteryVoltage: Double,
    @SerialName("gps_satellites")
    val gpsSatellites: Int,
    val pitch: Double,
    val roll: Double,
    val yaw: Double,
    @SerialName("wind_speed")
    val windSpeed: Double,
    val humidity: Double
)

@Serializable
data class AIAnalysisResponse(
    @SerialName("analysis_id")
    val analysisId: String,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("anomalies")
    val anomalies: List<AnomalyResponse> = emptyList(),
    @SerialName("predictions")
    val predictions: List<PredictionResponse> = emptyList(),
    @SerialName("risk_score")
    val riskScore: Double,
    @SerialName("recommendations")
    val recommendations: List<String> = emptyList(),
    @SerialName("analysis_time_ms")
    val analysisTime: Long
) {
    fun toDomain(): AIAnalysis = AIAnalysis(
        id = analysisId,
        timestamp = timestamp,
        anomalies = anomalies.map { it.toDomain() },
        predictions = predictions.map { it.toDomain() },
        riskScore = riskScore,
        recommendations = recommendations,
        analysisTime = analysisTime
    )
}

@Serializable
data class AnomalyResponse(
    @SerialName("anomaly_id")
    val anomalyId: String,
    @SerialName("category")
    val category: String,
    @SerialName("confidence")
    val confidence: Double,
    @SerialName("description")
    val description: String,
    @SerialName("affected_metric")
    val affectedMetric: String,
    @SerialName("normal_value")
    val normalValue: Double,
    @SerialName("actual_value")
    val actualValue: Double,
    @SerialName("severity")
    val severity: String
) {
    fun toDomain(): Anomaly = Anomaly(
        id = anomalyId,
        category = category,
        confidence = confidence,
        description = description,
        affectedMetric = affectedMetric,
        normalValue = normalValue,
        actualValue = actualValue,
        severity = AlertSeverity.fromValue(severity)
    )
}

@Serializable
data class PredictionResponse(
    @SerialName("prediction_id")
    val predictionId: String,
    @SerialName("component")
    val component: String,
    @SerialName("failure_type")
    val failureType: String,
    @SerialName("probability")
    val probability: Double,
    @SerialName("estimated_time_to_failure_ms")
    val estimatedTimeToFailure: Long,
    @SerialName("severity")
    val severity: String,
    @SerialName("suggested_actions")
    val suggestedActions: List<String> = emptyList()
) {
    fun toDomain(): Prediction = Prediction(
        id = predictionId,
        component = component,
        failureType = failureType,
        probability = probability,
        estimatedTimeToFailure = estimatedTimeToFailure,
        severity = AlertSeverity.fromValue(severity),
        suggestedActions = suggestedActions
    )
}

/**
 * Generic API response wrapper for type-safe handling.
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: T? = null,
    @SerialName("error")
    val error: String? = null,
    @SerialName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Drone control command request
 */
@Serializable
data class DroneCommandRequest(
    @SerialName("drone_id")
    val droneId: String,
    @SerialName("command")
    val command: String,
    @SerialName("parameters")
    val parameters: Map<String, String> = emptyMap()
)

/**
 * Drone control command response
 */
@Serializable
data class DroneCommandResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("execution_time_ms")
    val executionTime: Long,
    @SerialName("drone_state")
    val droneState: String
)