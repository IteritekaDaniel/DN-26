package com.example.dn_26.data.models

import java.util.UUID

/**
 * 📊 PROFESSIONAL DATA MODELS v2.0
 */

data class FlightLog(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val duration: String,
    val distance: String,
    val maxAltitude: Float,
    val avgSpeed: Float,
    val batteryUsed: Int,
    val startLocation: String = "Home Base",
    val endLocation: String = "Home Base"
)

data class StatisticsModel(
    val totalFlights: Int = 0,
    val totalDistance: Float = 0f,
    val totalFlightTime: String = "0h 0m",
    val maxAltitudeReached: Float = 0f,
    val averageBatteryHealth: Int = 100,
    val safetyScore: Int = 95
)

data class DroneSystemHealth(
    val motorStatus: String = "Optimal",
    val sensorStatus: String = "Calibrated",
    val radioSignal: Int = 100,
    val cpuTemp: Float = 35.0f,
    val internalMemory: String = "14.2 GB Free"
)
