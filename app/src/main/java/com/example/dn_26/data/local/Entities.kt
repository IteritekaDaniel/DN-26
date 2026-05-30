package com.example.dn_26.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dn_26.domain.model.Alert
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.domain.model.AlertType
import com.example.dn_26.domain.model.Telemetry

@Entity(tableName = "telemetry_history")
data class TelemetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val altitude: Double,
    val speed: Double,
    val temperature: Double,
    val batteryVoltage: Double,
    val gpsSatellites: Int,
    val gpsSignalStrength: Int,
    val pitch: Double,
    val roll: Double,
    val yaw: Double,
    val windSpeed: Double,
    val humidity: Double
) {
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

    companion object {
        fun fromDomain(domain: Telemetry): TelemetryEntity = TelemetryEntity(
            timestamp = domain.timestamp,
            altitude = domain.altitude,
            speed = domain.speed,
            temperature = domain.temperature,
            batteryVoltage = domain.batteryVoltage,
            gpsSatellites = domain.gpsSatellites,
            gpsSignalStrength = domain.gpsSignalStrength,
            pitch = domain.pitch,
            roll = domain.roll,
            yaw = domain.yaw,
            windSpeed = domain.windSpeed,
            humidity = domain.humidity
        )
    }
}

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val severity: String,
    val type: String,
    val timestamp: Long,
    @ColumnInfo(name = "is_read") val isRead: Boolean,
    val code: String
) {
    fun toDomain(): Alert = Alert(
        id = id,
        title = title,
        message = message,
        severity = AlertSeverity.valueOf(severity),
        type = AlertType.valueOf(type),
        timestamp = timestamp,
        isRead = isRead,
        code = code
    )

    companion object {
        fun fromDomain(domain: Alert): AlertEntity = AlertEntity(
            id = domain.id,
            title = domain.title,
            message = domain.message,
            severity = domain.severity.name,
            type = domain.type.name,
            timestamp = domain.timestamp,
            isRead = domain.isRead,
            code = domain.code
        )
    }
}
