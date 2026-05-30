package com.example.dn_26.ai

import com.example.dn_26.data.repository.AIRepository
import com.example.dn_26.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

class AIService(
    private val aiRepository: AIRepository
) {
    private var baselineMetrics: BaselineMetrics? = null

    fun initializeBaseline(telemetry: Telemetry) {
        baselineMetrics = BaselineMetrics(
            baselineTemp = telemetry.temperature,
            baselineBattery = telemetry.batteryVoltage
        )
    }

    suspend fun detectAnomalies(
        droneId: String,
        telemetry: Telemetry
    ): Flow<Anomaly> = flow {
        // Local detection
        val baseline = baselineMetrics
        if (baseline != null && telemetry.temperature > baseline.baselineTemp + 15) {
            emit(Anomaly(
                id = UUID.randomUUID().toString(),
                category = "Temperature_Rise",
                confidence = 0.85,
                description = "Abnormal temperature increase",
                severity = AlertSeverity.MEDIUM
            ))
        }

        // Remote detection
        aiRepository.analyzeTelemetry(droneId, telemetry).onSuccess { analysis ->
            analysis.anomalies.forEach { emit(it) }
        }
    }

    suspend fun predictFailures(droneId: String, telemetry: Telemetry): Result<List<Prediction>> {
        return aiRepository.analyzeTelemetry(droneId, telemetry).map { it.predictions }
    }

    suspend fun getActionableRecommendations(droneId: String, telemetry: Telemetry): Result<List<String>> {
        return aiRepository.getRecommendations(droneId, telemetry)
    }

    fun analyzeOnDevice(
        droneId: String,
        telemetry: Telemetry,
        recentTelemetry: List<Telemetry>
    ): AIAnalysis {
        val anomalies = mutableListOf<Anomaly>()
        val predictions = mutableListOf<Prediction>()
        val recommendations = mutableListOf<String>()
        val batteryPercent = ((telemetry.batteryVoltage / 12.6) * 100.0).coerceIn(0.0, 100.0)
        val sampleWindow = recentTelemetry.takeLast(30)

        if (batteryPercent < 20) {
            anomalies += Anomaly(
                id = UUID.randomUUID().toString(),
                category = "Power",
                confidence = if (batteryPercent < 12) 0.96 else 0.82,
                description = "Battery reserve is below operational safety margin.",
                affectedMetric = "battery",
                normalValue = 35.0,
                actualValue = batteryPercent,
                severity = if (batteryPercent < 12) AlertSeverity.CRITICAL else AlertSeverity.MEDIUM
            )
            recommendations += "Land or trigger return-home before battery sag affects motor stability."
        }

        if (telemetry.temperature > 62.0) {
            anomalies += Anomaly(
                id = UUID.randomUUID().toString(),
                category = "Thermal",
                confidence = ((telemetry.temperature - 50.0) / 25.0).coerceIn(0.65, 0.98),
                description = "Thermal load is high for sustained flight.",
                affectedMetric = "temperature",
                normalValue = 45.0,
                actualValue = telemetry.temperature,
                severity = if (telemetry.temperature > 72.0) AlertSeverity.CRITICAL else AlertSeverity.MEDIUM
            )
            recommendations += "Reduce throttle demand and inspect airflow around ESC and motor bays."
        }

        if (telemetry.gpsSatellites in 1..7) {
            anomalies += Anomaly(
                id = UUID.randomUUID().toString(),
                category = "Navigation",
                confidence = 0.74,
                description = "GPS lock is weak for autonomous navigation.",
                affectedMetric = "gpsSatellites",
                normalValue = 12.0,
                actualValue = telemetry.gpsSatellites.toDouble(),
                severity = AlertSeverity.MEDIUM
            )
            recommendations += "Avoid autonomous mission modes until GPS lock is stable."
        }

        if (abs(telemetry.pitch) > 35 || abs(telemetry.roll) > 35) {
            anomalies += Anomaly(
                id = UUID.randomUUID().toString(),
                category = "Attitude",
                confidence = 0.81,
                description = "Aggressive attitude detected; payload or wind may be destabilizing the frame.",
                affectedMetric = "pitch_roll",
                normalValue = 15.0,
                actualValue = max(abs(telemetry.pitch), abs(telemetry.roll)),
                severity = AlertSeverity.MEDIUM
            )
            recommendations += "Switch to stabilize/alt-hold and check center of gravity."
        }

        val batteryDrop = if (sampleWindow.size >= 6) {
            val first = sampleWindow.first().batteryVoltage
            val last = sampleWindow.last().batteryVoltage
            first - last
        } else {
            0.0
        }
        if (batteryDrop > 0.35) {
            predictions += Prediction(
                id = UUID.randomUUID().toString(),
                component = "Battery Pack",
                failureType = "Voltage sag",
                probability = (batteryDrop / 0.8).coerceIn(0.45, 0.95),
                estimatedTimeToFailure = 180_000,
                severity = AlertSeverity.MEDIUM,
                suggestedActions = listOf("Reduce current draw", "Prepare landing", "Check cell balance after flight")
            )
        }

        val avgTemp = sampleWindow.map { it.temperature }.takeIf { it.isNotEmpty() }?.average() ?: telemetry.temperature
        if (avgTemp > 58.0 && telemetry.speed > 15.0) {
            predictions += Prediction(
                id = UUID.randomUUID().toString(),
                component = "Propulsion",
                failureType = "Thermal stress",
                probability = ((avgTemp - 50.0) / 30.0).coerceIn(0.35, 0.9),
                estimatedTimeToFailure = 420_000,
                severity = if (avgTemp > 70.0) AlertSeverity.CRITICAL else AlertSeverity.MEDIUM,
                suggestedActions = listOf("Lower speed envelope", "Inspect propellers", "Verify motor temperature after landing")
            )
        }

        if (recommendations.isEmpty()) {
            recommendations += "Telemetry is inside nominal envelope. Continue monitoring battery, GPS and motor temperature."
        }

        val anomalyRisk = anomalies.sumOf {
            when (it.severity) {
                AlertSeverity.CRITICAL -> 32.0
                AlertSeverity.MEDIUM -> 18.0
                AlertSeverity.LOW -> 8.0
            } * it.confidence
        }
        val predictionRisk = predictions.sumOf { it.probability * 18.0 }
        val riskScore = (anomalyRisk + predictionRisk).coerceIn(0.0, 100.0)

        return AIAnalysis(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            anomalies = anomalies,
            predictions = predictions,
            riskScore = riskScore,
            recommendations = recommendations.distinct(),
            analysisTime = 0L
        )
    }

    private data class BaselineMetrics(
        val baselineTemp: Double,
        val baselineBattery: Double
    )
}
