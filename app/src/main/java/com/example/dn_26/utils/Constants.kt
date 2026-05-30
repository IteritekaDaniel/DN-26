package com.example.dn_26.utils

/**
 * Application-wide constants and configuration values.
 */
object AppConstants {
    // Drone configuration
    const val DRONE_ID = "DRONE-001"
    const val DRONE_NAME = "DroneX Pro"

    // Telemetry configuration
    const val TELEMETRY_UPDATE_INTERVAL_MS = 100L  // 10 Hz
    const val TELEMETRY_HISTORY_WINDOW_MS = 300000L  // 5 minutes

    // Alert configuration
    const val ALERT_DEBOUNCE_MS = 5000L  // Minimum time between same alert types
    const val MAX_ALERT_HISTORY = 100

    // API configuration
    const val API_TIMEOUT_SECONDS = 15L
    const val API_RETRY_COUNT = 3

    // UI configuration
    const val ANIMATION_DURATION_MS = 300
    const val BUTTON_DEBOUNCE_MS = 200L

    // Thresholds
    const val MAX_ALTITUDE = 120.0  // meters
    const val MAX_FLIGHT_TIME = 27 * 60  // 27 minutes in seconds
    const val MAX_WIND_SPEED = 12.0  // m/s
}