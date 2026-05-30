package com.example.dn_26.drone

import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Telemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlin.math.hypot
import kotlin.random.Random

class FakeDroneController : IDroneController {

    private val random = Random(System.currentTimeMillis())
    private val _droneState = MutableStateFlow(DroneState.DISCONNECTED)

    private var currentAlt = 0.0
    private var currentSpd = 0.0
    private var targetAlt = 0.0
    private var targetSpd = 0.0
    private var pitch = 0.0
    private var roll = 0.0
    private var yaw = 0.0

    private var battery = 100
    private var isSimulating = false
    private var isArmed = false
    private var isRecording = false

    override suspend fun initialize(): Result<Unit> = runCatching {
        delay(250)
        _droneState.value = DroneState.IDLE
    }

    override suspend fun connect(): Result<Unit> = runCatching {
        delay(350)
        _droneState.value = DroneState.CONNECTED
        isSimulating = true
    }

    override suspend fun disconnect(): Result<Unit> = runCatching {
        isSimulating = false
        isArmed = false
        targetAlt = 0.0
        targetSpd = 0.0
        _droneState.value = DroneState.DISCONNECTED
    }

    override suspend fun executeCommand(
        command: DroneCommand,
        parameters: Map<String, Any>
    ): Result<Unit> = runCatching {
        when (command) {
            DroneCommand.ARM -> {
                isArmed = true
                _droneState.value = DroneState.IDLE
            }
            DroneCommand.DISARM -> {
                isArmed = false
                targetAlt = 0.0
                targetSpd = 0.0
                _droneState.value = DroneState.IDLE
            }
            DroneCommand.TAKEOFF -> {
                isArmed = true
                _droneState.value = DroneState.TAKING_OFF
                delay(700)
                targetAlt = 15.0
                _droneState.value = DroneState.FLYING
            }
            DroneCommand.LAND -> {
                _droneState.value = DroneState.LANDING
                targetAlt = 0.0
                targetSpd = 0.0
                delay(900)
                isArmed = false
                _droneState.value = DroneState.LANDED
            }
            DroneCommand.EMERGENCY_STOP -> {
                targetAlt = 0.0
                targetSpd = 0.0
                isArmed = false
                _droneState.value = DroneState.EMERGENCY_STOP
            }
            DroneCommand.HOVER -> {
                targetSpd = 0.0
                pitch = 0.0
                roll = 0.0
            }
            DroneCommand.RETURN_HOME -> {
                isArmed = true
                targetSpd = 7.0
                yaw = 0.0
                if (_droneState.value != DroneState.FLYING) _droneState.value = DroneState.FLYING
            }
            DroneCommand.MOVE_FORWARD -> targetSpd = 8.0
            DroneCommand.MOVE_BACKWARD -> targetSpd = 4.0
            DroneCommand.MOVE_LEFT -> roll = -15.0
            DroneCommand.MOVE_RIGHT -> roll = 15.0
            DroneCommand.MOVE_UP -> targetAlt = (targetAlt + 1.0).coerceAtMost(120.0)
            DroneCommand.MOVE_DOWN -> targetAlt = (targetAlt - 1.0).coerceAtLeast(0.0)
            DroneCommand.ROTATE_CLOCKWISE -> yaw += 8.0
            DroneCommand.ROTATE_COUNTER_CLOCKWISE -> yaw -= 8.0
            DroneCommand.CALIBRATE -> {
                pitch = 0.0
                roll = 0.0
                yaw = 0.0
            }
            DroneCommand.START_RECORDING -> isRecording = true
            DroneCommand.STOP_RECORDING -> isRecording = false
        }
    }

    override suspend fun updateJoystickInput(
        x: Float,
        y: Float,
        z: Float,
        rotation: Float
    ): Result<Unit> = runCatching {
        if (!isArmed && _droneState.value != DroneState.FLYING) return@runCatching

        pitch = y.toDouble() * 30.0
        roll = x.toDouble() * 30.0
        yaw += rotation.toDouble() * 5.0
        targetAlt = (targetAlt + (-z.toDouble() * 0.5)).coerceIn(0.0, 120.0)
        targetSpd = hypot(x.toDouble(), y.toDouble()) * 25.0
    }

    override fun observeTelemetry(): Flow<Telemetry> = flow {
        while (true) {
            if (_droneState.value != DroneState.DISCONNECTED) {
                currentAlt += (targetAlt - currentAlt) * 0.08
                currentSpd += (targetSpd - currentSpd) * 0.08

                if (isSimulating && (_droneState.value == DroneState.FLYING || isArmed)) {
                    battery = (battery - if (isRecording) 1 else 0).coerceAtLeast(8)
                }

                val noise = random.nextDouble(-0.18, 0.18)
                emit(
                    Telemetry(
                        timestamp = System.currentTimeMillis(),
                        altitude = (currentAlt + noise).coerceAtLeast(0.0),
                        speed = (currentSpd + noise * 2).coerceAtLeast(0.0),
                        temperature = 27.0 + currentSpd * 0.16 + if (isRecording) 1.5 else 0.0,
                        batteryVoltage = 12.6 * (battery / 100.0),
                        gpsSatellites = 16,
                        gpsSignalStrength = 92,
                        pitch = pitch + random.nextDouble(-0.8, 0.8),
                        roll = roll + random.nextDouble(-0.8, 0.8),
                        yaw = ((yaw % 360) + 360) % 360,
                        windSpeed = 4.5 + random.nextDouble(0.0, 4.0),
                        humidity = 48.0
                    )
                )
            }
            delay(100)
        }
    }

    override fun observeState(): Flow<DroneState> = _droneState.asStateFlow()
    override suspend fun getBatteryLevel(): Result<Int> = Result.success(battery)
    override suspend fun isConnected(): Result<Boolean> = Result.success(_droneState.value != DroneState.DISCONNECTED)
    override suspend fun calibrate(): Result<Unit> = executeCommand(DroneCommand.CALIBRATE)
    override suspend fun emergencyStop(): Result<Unit> = executeCommand(DroneCommand.EMERGENCY_STOP)
}
