package com.example.dn_26.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

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

data class VisionFinding(
    val title: String,
    val description: String,
    val severity: AlertSeverity = AlertSeverity.LOW,
    val confidence: Double = 0.0
)

data class VisionQuestion(
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class VisionState(
    val streamUrl: String = "",
    val snapshotUrl: String = "",
    val isAnalyzing: Boolean = false,
    val autoAnalyze: Boolean = false,
    val lastFrameTimestamp: Long = 0L,
    val brightness: Double = 0.0,
    val contrast: Double = 0.0,
    val sharpness: Double = 0.0,
    val frameScore: Int = 0,
    val findings: List<VisionFinding> = emptyList(),
    val qaHistory: List<VisionQuestion> = emptyList(),
    val error: String? = null
)

class VisionAIViewModel : ViewModel() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(VisionState())
    val state: StateFlow<VisionState> = _state.asStateFlow()

    fun updateUrls(streamUrl: String, snapshotUrl: String) {
        _state.update {
            it.copy(
                streamUrl = streamUrl.trim(),
                snapshotUrl = snapshotUrl.trim(),
                error = null
            )
        }
    }

    fun setAutoAnalyze(enabled: Boolean) {
        _state.update { it.copy(autoAnalyze = enabled) }
    }

    fun analyzeSnapshot(snapshotUrl: String = _state.value.snapshotUrl) {
        if (snapshotUrl.isBlank()) {
            _state.update { it.copy(error = "Snapshot URL is empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true, error = null) }
            runCatching {
                val bitmap = fetchBitmap(snapshotUrl)
                analyzeBitmap(bitmap)
            }.onSuccess { analysis ->
                _state.update {
                    it.copy(
                        isAnalyzing = false,
                        lastFrameTimestamp = System.currentTimeMillis(),
                        brightness = analysis.brightness,
                        contrast = analysis.contrast,
                        sharpness = analysis.sharpness,
                        frameScore = analysis.frameScore,
                        findings = analysis.findings,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isAnalyzing = false,
                        error = error.message ?: "Vision analysis failed"
                    )
                }
            }
        }
    }

    fun askQuestion(question: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank()) return

        val current = _state.value
        val answer = buildVisionAnswer(cleanQuestion, current)
        _state.update {
            it.copy(
                qaHistory = (listOf(VisionQuestion(cleanQuestion, answer)) + it.qaHistory).take(8)
            )
        }
    }

    private suspend fun fetchBitmap(url: String): Bitmap = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Snapshot request failed: HTTP ${response.code}")
            val bytes = response.body?.bytes() ?: error("Snapshot response is empty")
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: error("Snapshot is not a decodable image")
        }
    }

    private fun analyzeBitmap(bitmap: Bitmap): VisionFrameAnalysis {
        val width = bitmap.width
        val height = bitmap.height
        val step = (maxOf(width, height) / 72).coerceAtLeast(1)
        var count = 0
        var sum = 0.0
        var sumSquare = 0.0
        var edgeSum = 0.0
        var redOrangeCount = 0

        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                val luminance = red * 0.299 + green * 0.587 + blue * 0.114
                sum += luminance
                sumSquare += luminance * luminance

                if (x + step < width) {
                    edgeSum += abs(luminance - bitmap.getPixel(x + step, y).luma())
                }
                if (y + step < height) {
                    edgeSum += abs(luminance - bitmap.getPixel(x, y + step).luma())
                }

                if (red > 150 && red > green * 1.25 && green > blue * 1.1) {
                    redOrangeCount++
                }
                count++
            }
        }

        val safeCount = count.coerceAtLeast(1)
        val avg = sum / safeCount
        val variance = (sumSquare / safeCount) - avg * avg
        val brightness = (avg / 255.0).coerceIn(0.0, 1.0)
        val contrast = (sqrt(variance.coerceAtLeast(0.0)) / 128.0).coerceIn(0.0, 1.0)
        val sharpness = (edgeSum / (safeCount * 255.0)).coerceIn(0.0, 1.0)
        val redOrangeRatio = redOrangeCount / safeCount.toDouble()

        val findings = mutableListOf<VisionFinding>()
        if (brightness < 0.08 && contrast < 0.12) {
            findings += VisionFinding(
                title = "Possible lens obstruction",
                description = "The frame is extremely dark with very low contrast.",
                severity = AlertSeverity.CRITICAL,
                confidence = 0.88
            )
        } else if (brightness < 0.18) {
            findings += VisionFinding(
                title = "Low visibility",
                description = "The camera image is too dark for reliable visual navigation.",
                severity = AlertSeverity.MEDIUM,
                confidence = 0.78
            )
        }
        if (brightness > 0.88) {
            findings += VisionFinding(
                title = "Overexposure",
                description = "The image is washed out; obstacle and landing-zone detail may be lost.",
                severity = AlertSeverity.MEDIUM,
                confidence = 0.76
            )
        }
        if (sharpness < 0.07 && brightness > 0.16) {
            findings += VisionFinding(
                title = "Blur or vibration",
                description = "Low edge energy suggests motion blur, lens focus issue, or frame vibration.",
                severity = AlertSeverity.MEDIUM,
                confidence = 0.72
            )
        }
        if (contrast < 0.11 && brightness in 0.18..0.82) {
            findings += VisionFinding(
                title = "Low contrast scene",
                description = "Scene contrast is weak; visual anomaly detection will be less confident.",
                severity = AlertSeverity.LOW,
                confidence = 0.66
            )
        }
        if (redOrangeRatio > 0.22) {
            findings += VisionFinding(
                title = "Red/orange dominance",
                description = "Large warm-color regions detected. Verify if this is terrain, warning light, heat source, or fire-like object.",
                severity = AlertSeverity.MEDIUM,
                confidence = redOrangeRatio.coerceIn(0.55, 0.92)
            )
        }

        var penalty = 0
        for (finding in findings) {
            penalty += when (finding.severity) {
                AlertSeverity.CRITICAL -> 34
                AlertSeverity.MEDIUM -> 18
                AlertSeverity.LOW -> 9
            }
        }
        val frameScore = (100 - penalty).coerceIn(0, 100)
        return VisionFrameAnalysis(brightness, contrast, sharpness, frameScore, findings)
    }

    private fun buildVisionAnswer(question: String, state: VisionState): String {
        val q = question.lowercase()
        if (state.lastFrameTimestamp == 0L) {
            return "I need at least one analyzed snapshot first. Tap Analyze Frame or enable Auto AI."
        }

        val strongestFinding = state.findings.maxByOrNull { it.confidence }
        return when {
            q.contains("danger") || q.contains("risk") || q.contains("anomal") ->
                if (state.findings.isEmpty()) {
                    "No visual anomaly is detected in the latest analyzed frame. Frame quality score is ${state.frameScore}/100."
                } else {
                    "Main visual concern: ${strongestFinding?.title}. ${strongestFinding?.description} Frame quality score is ${state.frameScore}/100."
                }
            q.contains("flou") || q.contains("blur") || q.contains("vibration") ->
                "Sharpness is ${state.sharpness.formatPercent()}. ${state.findings.firstOrNull { it.title.contains("Blur") }?.description ?: "No strong blur/vibration signature is detected."}"
            q.contains("lumi") || q.contains("dark") || q.contains("night") || q.contains("exposure") ->
                "Brightness is ${state.brightness.formatPercent()} and contrast is ${state.contrast.formatPercent()}. ${state.findings.firstOrNull { it.title.contains("visibility") || it.title.contains("Overexposure") || it.title.contains("obstruction") }?.description ?: "Exposure looks usable for basic FPV monitoring."}"
            q.contains("enregistr") || q.contains("record") || q.contains("video") ->
                "Recording is controlled from the FPV screen with the REC button. For full real-time DVR, the ESP32 or companion camera should write the stream to SD/storage while the app sends START_RECORDING and STOP_RECORDING."
            q.contains("api") || q.contains("model") || q.contains("objet") ->
                "This app now performs local image-quality and anomaly heuristics. For object detection, cracks, people, smoke, or vehicle recognition, plug a vision API or an on-device TensorFlow Lite model into this VisionAIViewModel."
            else ->
                "Latest frame score is ${state.frameScore}/100. Brightness ${state.brightness.formatPercent()}, contrast ${state.contrast.formatPercent()}, sharpness ${state.sharpness.formatPercent()}. ${strongestFinding?.description ?: "No visual anomaly detected."}"
        }
    }

    private fun Int.luma(): Double {
        val red = (this shr 16) and 0xFF
        val green = (this shr 8) and 0xFF
        val blue = this and 0xFF
        return red * 0.299 + green * 0.587 + blue * 0.114
    }

    private fun Double.formatPercent(): String = "${(this * 100).toInt()}%"

    private data class VisionFrameAnalysis(
        val brightness: Double,
        val contrast: Double,
        val sharpness: Double,
        val frameScore: Int,
        val findings: List<VisionFinding>
    )
}
