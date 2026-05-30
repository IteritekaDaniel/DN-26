package com.example.dn_26.alert

import com.example.dn_26.domain.model.Alert
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.domain.model.AlertType
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.data.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AlertEngine(
    private val alertRepository: AlertRepository,
    private val thresholds: AlertThresholds = AlertThresholds()
) {
    private val lastAlertTime = mutableMapOf<String, Long>()
    private val alertDebounceMs = 5000L

    suspend fun evaluateTelemetry(telemetry: Telemetry): Flow<Alert> = flow {
        val alerts = mutableListOf<Alert>()
        val batteryPercentage = (telemetry.batteryVoltage / 12.6) * 100
        
        if (batteryPercentage <= thresholds.batteryLowCritical) {
            alerts.add(createAlert(AlertType.BATTERY, "Critical Battery", "Battery at ${batteryPercentage.toInt()}%. Land now.", AlertSeverity.CRITICAL))
        } else if (batteryPercentage <= thresholds.batteryLow) {
            alerts.add(createAlert(AlertType.BATTERY, "Low Battery", "Battery at ${batteryPercentage.toInt()}%.", AlertSeverity.MEDIUM))
        }

        if (telemetry.temperature > thresholds.tempCritical) {
            alerts.add(createAlert(AlertType.THERMAL, "Critical Temp", "${telemetry.temperature.toInt()}°C. Stop.", AlertSeverity.CRITICAL))
        }

        for (alert in alerts) {
            if (shouldEmitAlert(alert)) {
                lastAlertTime[alert.type.toString()] = System.currentTimeMillis()
                alertRepository.emitAlert(alert)
                emit(alert)
            }
        }
    }

    private fun shouldEmitAlert(alert: Alert): Boolean {
        val lastTime = lastAlertTime[alert.type.toString()] ?: 0L
        return System.currentTimeMillis() - lastTime >= alertDebounceMs
    }

    private fun createAlert(type: AlertType, title: String, message: String, severity: AlertSeverity): Alert = Alert(
        id = UUID.randomUUID().toString(),
        title = title,
        message = message,
        severity = severity,
        type = type,
        timestamp = System.currentTimeMillis(),
        isRead = false,
        code = "${type.name}-${severity.name}"
    )
}

data class AlertThresholds(
    val batteryLowCritical: Int = 10,
    val batteryLow: Int = 25,
    val tempCritical: Double = 65.0,
    val tempWarning: Double = 55.0,
    val minGpsSatellites: Int = 8,
    val maxWindSpeed: Double = 12.0
)