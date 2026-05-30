package com.example.dn_26.drone

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Telemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

class Esp32BluetoothDroneController(
    private val context: Context
) : IDroneController, ConfigurableDroneController {

    private val _state = MutableStateFlow(DroneState.DISCONNECTED)
    private var profile = ConnectionProfile()
    private var socket: BluetoothSocket? = null
    private var output: OutputStream? = null
    private var reader: BufferedReader? = null
    private var lastTelemetry = Telemetry()
    private var lastBatteryLevel = 100

    override suspend fun configureConnection(profile: ConnectionProfile): Result<Unit> = runCatching {
        disconnect()
        this.profile = profile
    }

    override fun currentConnectionProfile(): ConnectionProfile = profile

    override suspend fun initialize(): Result<Unit> = runCatching {
        _state.value = DroneState.INITIALIZING
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(): Result<Unit> = runCatching {
        ensureBluetoothPermission()
        val address = profile.bluetoothAddress.trim()
        require(address.isNotBlank()) { "Bluetooth MAC address is required" }

        withContext(Dispatchers.IO) {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: error("Bluetooth adapter not available")
            require(adapter.isEnabled) { "Bluetooth is disabled" }

            val device = adapter.getRemoteDevice(address)
            adapter.cancelDiscovery()
            val newSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            newSocket.connect()
            socket = newSocket
            output = newSocket.outputStream
            reader = BufferedReader(InputStreamReader(newSocket.inputStream))
        }

        _state.value = DroneState.CONNECTED
    }

    override suspend fun disconnect(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            runCatching { reader?.close() }
            runCatching { output?.close() }
            runCatching { socket?.close() }
            reader = null
            output = null
            socket = null
        }
        _state.value = DroneState.DISCONNECTED
    }

    override suspend fun executeCommand(
        command: DroneCommand,
        parameters: Map<String, Any>
    ): Result<Unit> = runCatching {
        writeJson(
            JSONObject()
                .put("type", "command")
                .put("command", command.toBluetoothCommand())
                .put("intensity", (parameters["intensity"] as? Number)?.toFloat() ?: 1f)
                .put("timestamp", System.currentTimeMillis())
        )

        _state.value = when (command) {
            DroneCommand.TAKEOFF -> DroneState.FLYING
            DroneCommand.LAND -> DroneState.LANDING
            DroneCommand.DISARM -> DroneState.IDLE
            DroneCommand.EMERGENCY_STOP -> DroneState.EMERGENCY_STOP
            else -> if (_state.value == DroneState.CONNECTED) DroneState.IDLE else _state.value
        }
    }

    override suspend fun updateJoystickInput(
        x: Float,
        y: Float,
        z: Float,
        rotation: Float
    ): Result<Unit> = runCatching {
        if (_state.value == DroneState.DISCONNECTED || _state.value == DroneState.EMERGENCY_STOP) return@runCatching
        val intensity = max(max(abs(x), abs(y)), max(abs(z), abs(rotation)))
        writeJson(
            JSONObject()
                .put("type", "joystick")
                .put("x", x)
                .put("y", y)
                .put("z", z)
                .put("rotation", rotation)
                .put("intensity", intensity)
                .put("timestamp", System.currentTimeMillis())
        )
    }

    override fun observeTelemetry(): Flow<Telemetry> = flow {
        while (currentCoroutineContext().isActive) {
            val line = withContext(Dispatchers.IO) { runCatching { reader?.readLine() }.getOrNull() }
            if (!line.isNullOrBlank()) {
                val telemetry = runCatching { JSONObject(line).toTelemetry() }.getOrNull()
                if (telemetry != null) {
                    lastTelemetry = telemetry
                    lastBatteryLevel = ((telemetry.batteryVoltage / 12.6) * 100.0).toInt().coerceIn(0, 100)
                    emit(telemetry)
                }
            } else {
                delay(250)
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun observeState(): Flow<DroneState> = _state.asStateFlow()
    override suspend fun getBatteryLevel(): Result<Int> = Result.success(lastBatteryLevel)
    override suspend fun isConnected(): Result<Boolean> = Result.success(socket?.isConnected == true)
    override suspend fun calibrate(): Result<Unit> = executeCommand(DroneCommand.CALIBRATE)
    override suspend fun emergencyStop(): Result<Unit> = executeCommand(DroneCommand.EMERGENCY_STOP)

    private suspend fun writeJson(payload: JSONObject) {
        withContext(Dispatchers.IO) {
            val stream = output ?: error("Bluetooth socket is not connected")
            stream.write(payload.toString().toByteArray(Charsets.UTF_8))
            stream.write('\n'.code)
            stream.flush()
        }
    }

    private fun ensureBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("BLUETOOTH_CONNECT permission is required")
        }
    }

    private fun JSONObject.toTelemetry(): Telemetry {
        return Telemetry(
            timestamp = optLong("timestamp", System.currentTimeMillis()),
            altitude = optDouble("altitude", lastTelemetry.altitude),
            speed = optDouble("speed", lastTelemetry.speed),
            temperature = optDouble("temperature", lastTelemetry.temperature),
            batteryVoltage = optDouble("battery_voltage", optDouble("batteryVoltage", lastTelemetry.batteryVoltage)),
            gpsSatellites = optInt("gps_satellites", optInt("satellites", lastTelemetry.gpsSatellites)),
            gpsSignalStrength = optInt("gps_signal", optInt("gpsSignalStrength", lastTelemetry.gpsSignalStrength)),
            pitch = optDouble("pitch", lastTelemetry.pitch),
            roll = optDouble("roll", lastTelemetry.roll),
            yaw = optDouble("yaw", optDouble("heading", lastTelemetry.yaw)),
            windSpeed = optDouble("wind_speed", optDouble("windSpeed", lastTelemetry.windSpeed)),
            humidity = optDouble("humidity", lastTelemetry.humidity)
        )
    }

    private fun DroneCommand.toBluetoothCommand(): String = when (this) {
        DroneCommand.ARM -> "arm"
        DroneCommand.DISARM -> "disarm"
        DroneCommand.TAKEOFF -> "takeoff"
        DroneCommand.LAND -> "land"
        DroneCommand.HOVER -> "hover"
        DroneCommand.EMERGENCY_STOP -> "emergency_stop"
        DroneCommand.MOVE_FORWARD -> "forward"
        DroneCommand.MOVE_BACKWARD -> "backward"
        DroneCommand.MOVE_LEFT -> "left"
        DroneCommand.MOVE_RIGHT -> "right"
        DroneCommand.MOVE_UP -> "up"
        DroneCommand.MOVE_DOWN -> "down"
        DroneCommand.ROTATE_CLOCKWISE -> "yaw_right"
        DroneCommand.ROTATE_COUNTER_CLOCKWISE -> "yaw_left"
        DroneCommand.CALIBRATE -> "calibrate"
        DroneCommand.RETURN_HOME -> "return_home"
        DroneCommand.START_RECORDING -> "record_start"
        DroneCommand.STOP_RECORDING -> "record_stop"
    }

    private companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
