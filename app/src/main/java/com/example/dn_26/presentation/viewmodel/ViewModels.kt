package com.example.dn_26.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dn_26.ai.AIService
import com.example.dn_26.alert.AlertEngine
import com.example.dn_26.data.connectivity.ConnectionStatus
import com.example.dn_26.data.connectivity.MotorStatus
import com.example.dn_26.data.repository.AlertRepository
import com.example.dn_26.data.repository.DroneRepository
import com.example.dn_26.data.repository.TelemetryRepository
import com.example.dn_26.domain.model.Alert
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.domain.model.Anomaly
import com.example.dn_26.domain.model.ConnectionMode
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Prediction
import com.example.dn_26.domain.model.Telemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

data class ControllerTuning(
    val preset: String = "PRECISION",
    val sensitivity: Float = 0.72f,
    val deadZone: Float = 0.08f,
    val expo: Float = 0.35f
)

data class StickSignal(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val throttle: Float = 0f,
    val yaw: Float = 0f
)

data class CommandLogItem(
    val label: String,
    val timestamp: Long = System.currentTimeMillis(),
    val accepted: Boolean = true
)

data class DroneControlState(
    val droneState: DroneState = DroneState.DISCONNECTED,
    val connectionProfile: ConnectionProfile = ConnectionProfile(),
    val batteryLevel: Int = 100,
    val isConnecting: Boolean = false,
    val isArmed: Boolean = false,
    val isRecording: Boolean = false,
    val safetyLock: Boolean = false,
    val flightMode: String = "STABILIZE",
    val connectionError: String? = null,
    val statusMessage: String = "Ready",
    val lastCommand: DroneCommand? = null,
    val lastCommandTime: Long = 0,
    val commandInProgress: Boolean = false,
    val signalStrength: Int = 0,
    val latency: Long = 0,
    val connectionQuality: String = "OFFLINE",
    val motorHealth: String = "UNKNOWN",
    val uptime: Long = 0,
    val controllerTuning: ControllerTuning = ControllerTuning(),
    val lastStickSignal: StickSignal = StickSignal(),
    val commandHistory: List<CommandLogItem> = emptyList()
)

class DroneControlViewModel(
    private val droneRepository: DroneRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DroneControlState())
    val state: StateFlow<DroneControlState> = _state.asStateFlow()

    init {
        observeDroneState()
    }

    fun updateConnectionProfile(
        mode: ConnectionMode = _state.value.connectionProfile.mode,
        ipAddress: String = _state.value.connectionProfile.ipAddress,
        port: Int = _state.value.connectionProfile.port,
        bluetoothAddress: String = _state.value.connectionProfile.bluetoothAddress,
        bluetoothName: String = _state.value.connectionProfile.bluetoothName
    ) {
        _state.update {
            it.copy(
                connectionProfile = ConnectionProfile(
                    mode = mode,
                    ipAddress = ipAddress,
                    port = port.coerceIn(1, 65535),
                    bluetoothAddress = bluetoothAddress,
                    bluetoothName = bluetoothName
                ),
                connectionError = null
            )
        }
    }

    fun connectDrone(profile: ConnectionProfile = _state.value.connectionProfile) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isConnecting = true,
                    connectionProfile = profile,
                    connectionError = null,
                    statusMessage = "Connecting ${profile.mode.name.lowercase()}..."
                )
            }

            droneRepository.configureConnection(profile)
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            connectionError = error.message,
                            statusMessage = "Connection profile failed"
                        )
                    }
                    return@launch
                }

            droneRepository.initializeDrone()
                .onSuccess {
                    droneRepository.connectDrone()
                        .onSuccess {
                            updateBatteryLevel()
                            _state.update { current ->
                                current.copy(
                                    isConnecting = false,
                                    safetyLock = false,
                                    connectionError = null,
                                    connectionQuality = if (profile.mode == ConnectionMode.SIMULATION) "SIM" else "LINK",
                                    signalStrength = if (profile.mode == ConnectionMode.SIMULATION) 100 else current.signalStrength,
                                    statusMessage = "Connected via ${profile.mode.name.replace('_', ' ')}"
                                )
                            }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isConnecting = false,
                                    connectionError = error.message,
                                    statusMessage = "Connection failed"
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            connectionError = error.message,
                            statusMessage = "Initialization failed"
                        )
                    }
                }
        }
    }

    fun disconnectDrone() {
        viewModelScope.launch {
            droneRepository.disconnectDrone()
            _state.update {
                it.copy(
                    droneState = DroneState.DISCONNECTED,
                    isArmed = false,
                    isRecording = false,
                    safetyLock = false,
                    statusMessage = "Disconnected",
                    connectionQuality = "OFFLINE",
                    signalStrength = 0
                )
            }
        }
    }

    fun executeCommand(
        command: DroneCommand,
        parameters: Map<String, Any> = emptyMap()
    ) {
        val now = System.currentTimeMillis()
        if (command != DroneCommand.EMERGENCY_STOP && now - _state.value.lastCommandTime < 120) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    commandInProgress = true,
                    lastCommand = command,
                    statusMessage = "Sending ${command.name.lowercase()}..."
                )
            }

            droneRepository.executeCommand(command, parameters)
                .onSuccess {
                    _state.update { current ->
                        current.copy(
                            commandInProgress = false,
                            lastCommandTime = System.currentTimeMillis(),
                            isArmed = when (command) {
                                DroneCommand.ARM, DroneCommand.TAKEOFF, DroneCommand.RETURN_HOME -> true
                                DroneCommand.DISARM, DroneCommand.LAND, DroneCommand.EMERGENCY_STOP -> false
                                else -> current.isArmed
                            },
                            isRecording = when (command) {
                                DroneCommand.START_RECORDING -> true
                                DroneCommand.STOP_RECORDING -> false
                                else -> current.isRecording
                            },
                            safetyLock = if (command == DroneCommand.EMERGENCY_STOP) true else current.safetyLock,
                            commandHistory = appendCommandLog(current, command, accepted = true),
                            statusMessage = "${command.name.replace('_', ' ')} accepted"
                        )
                    }
                    updateBatteryLevel()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            commandInProgress = false,
                            connectionError = error.message,
                            commandHistory = appendCommandLog(it, command, accepted = false),
                            statusMessage = "Command failed"
                        )
                    }
                }
        }
    }

    fun updateJoystickInput(x: Float, y: Float, z: Float, rotation: Float) {
        if (_state.value.safetyLock) return
        val tuning = _state.value.controllerTuning
        val shaped = StickSignal(
            pitch = shapeAxis(x, tuning),
            roll = shapeAxis(y, tuning),
            throttle = shapeAxis(z, tuning),
            yaw = shapeAxis(rotation, tuning)
        )
        _state.update { it.copy(lastStickSignal = shaped) }
        viewModelScope.launch {
            droneRepository.updateJoystickInput(
                shaped.pitch,
                shaped.roll,
                shaped.throttle,
                shaped.yaw
            )
                .onFailure { error ->
                    _state.update { it.copy(connectionError = error.message) }
                }
        }
    }

    fun setControllerPreset(preset: String) {
        val tuning = when (preset.uppercase()) {
            "SPORT" -> ControllerTuning("SPORT", sensitivity = 1.0f, deadZone = 0.06f, expo = 0.18f)
            "CINEMA" -> ControllerTuning("CINEMA", sensitivity = 0.55f, deadZone = 0.1f, expo = 0.55f)
            "INSPECT" -> ControllerTuning("INSPECT", sensitivity = 0.62f, deadZone = 0.12f, expo = 0.62f)
            else -> ControllerTuning("PRECISION", sensitivity = 0.72f, deadZone = 0.08f, expo = 0.35f)
        }
        _state.update {
            it.copy(
                controllerTuning = tuning,
                statusMessage = "Controller preset ${tuning.preset}"
            )
        }
    }

    fun updateControllerTuning(
        sensitivity: Float = _state.value.controllerTuning.sensitivity,
        deadZone: Float = _state.value.controllerTuning.deadZone,
        expo: Float = _state.value.controllerTuning.expo
    ) {
        _state.update {
            it.copy(
                controllerTuning = it.controllerTuning.copy(
                    sensitivity = sensitivity.coerceIn(0.2f, 1.25f),
                    deadZone = deadZone.coerceIn(0.02f, 0.25f),
                    expo = expo.coerceIn(0f, 0.8f)
                )
            )
        }
    }

    fun setFlightMode(mode: String) {
        _state.update { it.copy(flightMode = mode.uppercase(), statusMessage = "Mode set to ${mode.uppercase()}") }
    }

    fun emergencyStop() {
        executeCommand(DroneCommand.EMERGENCY_STOP)
    }

    fun unlockSafety() {
        viewModelScope.launch {
            droneRepository.executeCommand(DroneCommand.DISARM)
            _state.update {
                it.copy(
                    safetyLock = false,
                    isArmed = false,
                    statusMessage = "Safety lock released; motors disarmed"
                )
            }
        }
    }

    fun updateConnectionMetrics(status: ConnectionStatus) {
        _state.update {
            it.copy(
                signalStrength = status.signalStrength,
                latency = status.latency,
                uptime = status.uptime,
                connectionQuality = when {
                    !status.isConnected -> "OFFLINE"
                    status.signalStrength >= 90 -> "EXCELLENT"
                    status.signalStrength >= 70 -> "GOOD"
                    status.signalStrength >= 40 -> "WARNING"
                    else -> "CRITICAL"
                }
            )
        }
    }

    fun updateMotorStatus(motors: MotorStatus) {
        _state.update { it.copy(motorHealth = motors.health) }
    }

    private fun observeDroneState() {
        viewModelScope.launch {
            droneRepository.observeDroneState().collect { droneState ->
                _state.update { current ->
                    current.copy(
                        droneState = droneState,
                        connectionQuality = when {
                            droneState == DroneState.DISCONNECTED -> "OFFLINE"
                            current.connectionProfile.mode == ConnectionMode.SIMULATION -> "SIM"
                            current.connectionQuality == "OFFLINE" -> "LINK"
                            else -> current.connectionQuality
                        }
                    )
                }
            }
        }
    }

    private fun updateBatteryLevel() {
        viewModelScope.launch {
            droneRepository.getBatteryLevel().onSuccess { level ->
                _state.update { it.copy(batteryLevel = level.coerceIn(0, 100)) }
            }
        }
    }

    private fun appendCommandLog(
        current: DroneControlState,
        command: DroneCommand,
        accepted: Boolean
    ): List<CommandLogItem> {
        return (
            listOf(
                CommandLogItem(
                    label = command.name.replace('_', ' '),
                    accepted = accepted
                )
            ) + current.commandHistory
        ).take(8)
    }

    private fun shapeAxis(value: Float, tuning: ControllerTuning): Float {
        val magnitude = abs(value)
        if (magnitude <= tuning.deadZone) return 0f
        val normalized = ((magnitude - tuning.deadZone) / (1f - tuning.deadZone)).coerceIn(0f, 1f)
        val linear = normalized
        val curved = normalized.pow(1f + tuning.expo * 2f)
        return sign(value) * (linear * (1f - tuning.expo) + curved * tuning.expo) * tuning.sensitivity
    }
}

data class TelemetryState(
    val latestTelemetry: Telemetry? = null,
    val recentTelemetry: List<Telemetry> = emptyList(),
    val isSampling: Boolean = false,
    val sampleCount: Int = 0,
    val isLoading: Boolean = false,
    val maxAltitude: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val batteryPercent: Int = 100,
    val verticalSpeed: Double = 0.0,
    val batteryDropPerMinute: Double = 0.0,
    val dataRateHz: Double = 0.0,
    val missionDurationMs: Long = 0L,
    val distanceEstimateMeters: Double = 0.0
)

class TelemetryViewModel(
    private val telemetryRepository: TelemetryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TelemetryState())
    val state: StateFlow<TelemetryState> = _state.asStateFlow()

    fun startTelemetryObservation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isSampling = true) }
            telemetryRepository.startTelemetryObservation().collect { telemetry ->
                _state.update { current ->
                    val samples = (current.recentTelemetry + telemetry).takeLast(180)
                    val previous = current.latestTelemetry
                    val deltaSeconds = previous?.let {
                        ((telemetry.timestamp - it.timestamp) / 1000.0).coerceAtLeast(0.1)
                    } ?: 0.1
                    val verticalSpeed = previous?.let {
                        (telemetry.altitude - it.altitude) / deltaSeconds
                    } ?: 0.0
                    val batteryDrop = if (samples.size >= 2) {
                        val first = samples.first()
                        val last = samples.last()
                        val minutes = ((last.timestamp - first.timestamp) / 60_000.0).coerceAtLeast(0.1)
                        (first.batteryVoltage - last.batteryVoltage).coerceAtLeast(0.0) / minutes
                    } else {
                        0.0
                    }
                    val duration = if (samples.isNotEmpty()) {
                        samples.last().timestamp - samples.first().timestamp
                    } else {
                        0L
                    }
                    current.copy(
                        latestTelemetry = telemetry,
                        recentTelemetry = samples,
                        sampleCount = current.sampleCount + 1,
                        isLoading = false,
                        maxAltitude = maxOf(current.maxAltitude, telemetry.altitude),
                        averageSpeed = samples.map { it.speed }.average().takeUnless { it.isNaN() } ?: 0.0,
                        batteryPercent = ((telemetry.batteryVoltage / 12.6) * 100.0).toInt().coerceIn(0, 100),
                        verticalSpeed = verticalSpeed,
                        batteryDropPerMinute = batteryDrop,
                        dataRateHz = current.sampleCount.coerceAtLeast(1) / (duration / 1000.0).coerceAtLeast(1.0),
                        missionDurationMs = duration,
                        distanceEstimateMeters = current.distanceEstimateMeters + telemetry.speed * deltaSeconds
                    )
                }
            }
        }
    }
}

data class AlertState(
    val alerts: List<Alert> = emptyList(),
    val unreadCount: Int = 0,
    val criticalCount: Int = 0,
    val recentAlerts: List<Alert> = emptyList()
)

class AlertViewModel(
    private val alertRepository: AlertRepository,
    private val alertEngine: AlertEngine
) : ViewModel() {

    private val _state = MutableStateFlow(AlertState())
    val state: StateFlow<AlertState> = _state.asStateFlow()

    init {
        observeAlerts()
    }

    fun evaluateTelemetry(telemetry: Telemetry) {
        viewModelScope.launch {
            alertEngine.evaluateTelemetry(telemetry).collect()
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            alertRepository.alerts.collect { alerts ->
                _state.update { currentState ->
                    currentState.copy(
                        alerts = alerts,
                        unreadCount = alerts.count { !it.isRead },
                        criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL },
                        recentAlerts = alerts.take(5)
                    )
                }
            }
        }
    }

    fun markAsRead(alertId: String) {
        viewModelScope.launch { alertRepository.markAsRead(alertId) }
    }
}

data class AIState(
    val anomalies: List<Anomaly> = emptyList(),
    val predictions: List<Prediction> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val riskScore: Double = 0.0,
    val isAnalyzing: Boolean = false,
    val lastAnalysisTimestamp: Long = 0L
)

class AIViewModel(
    private val aiService: AIService,
    private val droneId: String = "DRONE-001"
) : ViewModel() {
    private val _state = MutableStateFlow(AIState())
    val state: StateFlow<AIState> = _state.asStateFlow()

    fun analyzeTelemetry(
        telemetry: Telemetry,
        recentTelemetry: List<Telemetry> = emptyList()
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true) }
            val analysis = aiService.analyzeOnDevice(droneId, telemetry, recentTelemetry)
            _state.update {
                it.copy(
                    anomalies = analysis.anomalies,
                    predictions = analysis.predictions,
                    recommendations = analysis.recommendations,
                    riskScore = analysis.riskScore,
                    isAnalyzing = false,
                    lastAnalysisTimestamp = analysis.timestamp
                )
            }
        }
    }
}
