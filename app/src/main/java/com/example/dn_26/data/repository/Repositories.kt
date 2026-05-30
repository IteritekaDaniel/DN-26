package com.example.dn_26.data.repository

import com.example.dn_26.data.local.AlertDao
import com.example.dn_26.data.local.AlertEntity
import com.example.dn_26.data.local.TelemetryDao
import com.example.dn_26.data.local.TelemetryEntity
import com.example.dn_26.domain.model.Drone
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.domain.model.Alert
import com.example.dn_26.domain.model.AIAnalysis
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.drone.ConfigurableDroneController
import com.example.dn_26.drone.IDroneController
import com.example.dn_26.data.remote.AIApiService
import com.example.dn_26.data.model.AIAnalysisRequest
import com.example.dn_26.data.model.TelemetryPayload
import com.example.dn_26.data.models.FlightLog
import com.example.dn_26.data.models.StatisticsModel
import kotlinx.coroutines.flow.*

/**
 * Repository pattern implementation for drone data access.
 */
class DroneRepository(
    private val droneController: IDroneController
) {
    private val _droneState = MutableStateFlow<Drone>(Drone(isConnected = false))
    val droneState: StateFlow<Drone> = _droneState.asStateFlow()

    private val _flightLogs = MutableStateFlow<List<FlightLog>>(emptyList())
    val flightLogs: StateFlow<List<FlightLog>> = _flightLogs.asStateFlow()
    
    private val _statistics = MutableStateFlow(StatisticsModel())
    val statistics: StateFlow<StatisticsModel> = _statistics.asStateFlow()

    suspend fun configureConnection(profile: ConnectionProfile): Result<Unit> = runCatching {
        val configurable = droneController as? ConfigurableDroneController
            ?: error("Current drone controller does not support dynamic connection profiles")
        configurable.configureConnection(profile).getOrThrow()
    }

    fun currentConnectionProfile(): ConnectionProfile? {
        return (droneController as? ConfigurableDroneController)?.currentConnectionProfile()
    }

    suspend fun initializeDrone(): Result<Unit> = droneController.initialize()

    suspend fun connectDrone(): Result<Unit> = runCatching {
        droneController.connect().getOrThrow()
        _droneState.value = _droneState.value.copy(isConnected = true)
    }

    suspend fun disconnectDrone(): Result<Unit> = runCatching {
        droneController.disconnect().getOrThrow()
        _droneState.value = _droneState.value.copy(isConnected = false)
    }

    suspend fun executeCommand(
        command: DroneCommand,
        parameters: Map<String, Any> = emptyMap()
    ): Result<Unit> = droneController.executeCommand(command, parameters)

    suspend fun updateJoystickInput(x: Float, y: Float, z: Float, rotation: Float): Result<Unit> = 
        droneController.updateJoystickInput(x, y, z, rotation)

    fun observeTelemetry(): Flow<Telemetry> = droneController.observeTelemetry()

    fun observeDroneState(): Flow<DroneState> = droneController.observeState()

    suspend fun getBatteryLevel(): Result<Int> = droneController.getBatteryLevel()

    suspend fun isConnected(): Result<Boolean> = droneController.isConnected()

    suspend fun calibrateDrone(): Result<Unit> = droneController.calibrate()

    suspend fun emergencyStop(): Result<Unit> = droneController.emergencyStop()

    fun addFlightLog(log: FlightLog) {
        val currentLogs = _flightLogs.value.toMutableList()
        currentLogs.add(0, log)
        _flightLogs.value = currentLogs
        updateStatistics()
    }

    private fun updateStatistics() {
        val logs = _flightLogs.value
        if (logs.isEmpty()) return
        
        val totalDist = logs.sumOf { it.distance.replace(" km", "").toDoubleOrNull() ?: 0.0 }.toFloat()
        val maxAlt = logs.maxOf { it.maxAltitude }
        
        _statistics.value = StatisticsModel(
            totalFlights = logs.size,
            totalDistance = totalDist,
            totalFlightTime = "${logs.size * 20}m", // Mock calculation
            maxAltitudeReached = maxAlt
        )
    }
}

/**
 * Repository for telemetry data management with local persistence.
 */
class TelemetryRepository(
    private val droneRepository: DroneRepository,
    private val telemetryDao: TelemetryDao
) {
    private val _telemetryCache = MutableSharedFlow<Telemetry>(replay = 1)
    val telemetryCache: Flow<Telemetry> = _telemetryCache.asSharedFlow()

    fun startTelemetryObservation(): Flow<Telemetry> {
        return droneRepository.observeTelemetry()
            .distinctUntilChanged { old, new ->
                Math.abs(old.altitude - new.altitude) < 0.1 &&
                        Math.abs(old.speed - new.speed) < 0.5 &&
                        Math.abs(old.temperature - new.temperature) < 0.5
            }
            .onEach { telemetry ->
                _telemetryCache.emit(telemetry)
                telemetryDao.insertTelemetry(TelemetryEntity.fromDomain(telemetry))
            }
    }

    fun getRecentTelemetry(limit: Int): Flow<List<Telemetry>> {
        return telemetryDao.getRecentTelemetry(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

/**
 * Repository for alert data management with local persistence.
 */
class AlertRepository(
    private val alertDao: AlertDao
) {
    val alerts: Flow<List<Alert>> = alertDao.getAllAlerts().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun emitAlert(alert: Alert) {
        alertDao.insertAlert(AlertEntity.fromDomain(alert))
    }

    suspend fun markAsRead(alertId: String) {
        alertDao.markAsRead(alertId)
    }

    suspend fun clearAllAlerts() {
        alertDao.deleteAllAlerts()
    }
}

/**
 * Repository for AI analysis and anomaly detection.
 */
class AIRepository(
    private val apiService: AIApiService
) {
    suspend fun analyzeTelemetry(
        droneId: String,
        telemetry: Telemetry
    ): Result<AIAnalysis> = runCatching {
        val payload = TelemetryPayload(
            altitude = telemetry.altitude,
            speed = telemetry.speed,
            temperature = telemetry.temperature,
            batteryVoltage = telemetry.batteryVoltage,
            gpsSatellites = telemetry.gpsSatellites,
            pitch = telemetry.pitch,
            roll = telemetry.roll,
            yaw = telemetry.yaw,
            windSpeed = telemetry.windSpeed,
            humidity = telemetry.humidity
        )
        val request = AIAnalysisRequest(
            droneId = droneId,
            telemetryData = payload,
            analysisType = "comprehensive"
        )
        apiService.analyzeData(request).toDomain()
    }

    suspend fun getRecommendations(
        droneId: String,
        telemetry: Telemetry
    ): Result<List<String>> = runCatching {
        analyzeTelemetry(droneId, telemetry).getOrThrow().recommendations
    }
}
