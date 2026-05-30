package com.example.dn_26.drone

import com.example.dn_26.data.connectivity.BatteryStatus
import com.example.dn_26.data.connectivity.CommandPayload
import com.example.dn_26.data.connectivity.DroneAPI
import com.example.dn_26.data.connectivity.GPSData
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
import kotlinx.coroutines.withTimeout
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

class Esp32HttpDroneController : IDroneController, ConfigurableDroneController {

    private val _state = MutableStateFlow(DroneState.DISCONNECTED)
    private var profile = ConnectionProfile()
    private var api: DroneAPI? = null
    private var lastTelemetry = Telemetry()
    private var lastBatteryLevel = 100

    override suspend fun configureConnection(profile: ConnectionProfile): Result<Unit> = runCatching {
        disconnect()
        this.profile = profile
        api = createApi(profile.ipAddress, profile.port)
    }

    override fun currentConnectionProfile(): ConnectionProfile = profile

    override suspend fun initialize(): Result<Unit> = runCatching {
        if (api == null) api = createApi(profile.ipAddress, profile.port)
        _state.value = DroneState.INITIALIZING
    }

    override suspend fun connect(): Result<Unit> = runCatching {
        val droneApi = api ?: createApi(profile.ipAddress, profile.port).also { api = it }
        _state.value = DroneState.INITIALIZING
        withContext(Dispatchers.IO) {
            withTimeout(6_000) {
                droneApi.getHealth()
            }
        }
        _state.value = DroneState.CONNECTED
    }

    override suspend fun disconnect(): Result<Unit> = runCatching {
        _state.value = DroneState.DISCONNECTED
    }

    override suspend fun executeCommand(
        command: DroneCommand,
        parameters: Map<String, Any>
    ): Result<Unit> = runCatching {
        if (command == DroneCommand.EMERGENCY_STOP) {
            _state.value = DroneState.EMERGENCY_STOP
        }

        val payload = CommandPayload(
            command = command.toEsp32Command(),
            intensity = (parameters["intensity"] as? Number)?.toFloat() ?: 1f,
            x = (parameters["x"] as? Number)?.toFloat() ?: 0f,
            y = (parameters["y"] as? Number)?.toFloat() ?: 0f,
            z = (parameters["z"] as? Number)?.toFloat() ?: 0f,
            rotation = (parameters["rotation"] as? Number)?.toFloat() ?: 0f
        )
        withContext(Dispatchers.IO) {
            withTimeout(4_000) {
                requireApi().sendCommand(payload)
            }
        }

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
        withContext(Dispatchers.IO) {
            requireApi().sendCommand(
                CommandPayload(
                    command = "joystick",
                    intensity = intensity,
                    x = x,
                    y = y,
                    z = z,
                    rotation = rotation
                )
            )
        }
    }

    override fun observeTelemetry(): Flow<Telemetry> = flow {
        while (currentCoroutineContext().isActive) {
            if (_state.value != DroneState.DISCONNECTED) {
                try {
                    val droneApi = requireApi()
                    val telemetry = withContext(Dispatchers.IO) { droneApi.getTelemetry() }
                    val battery = runCatching { withContext(Dispatchers.IO) { droneApi.getBatteryStatus() } }.getOrNull()
                    val gps = runCatching { withContext(Dispatchers.IO) { droneApi.getGPSData() } }.getOrNull()
                    val mapped = telemetry.toTelemetry(battery, gps)
                    lastTelemetry = mapped
                    lastBatteryLevel = battery?.percentage?.toInt()
                        ?: ((mapped.batteryVoltage / 12.6) * 100.0).toInt().coerceIn(0, 100)
                    emit(mapped)
                } catch (error: Exception) {
                    if (_state.value != DroneState.EMERGENCY_STOP) _state.value = DroneState.ERROR
                    emit(lastTelemetry.copy(timestamp = System.currentTimeMillis()))
                    delay(1_000)
                }
            }
            delay(250)
        }
    }.flowOn(Dispatchers.IO)

    override fun observeState(): Flow<DroneState> = _state.asStateFlow()
    override suspend fun getBatteryLevel(): Result<Int> = Result.success(lastBatteryLevel)
    override suspend fun isConnected(): Result<Boolean> = Result.success(_state.value != DroneState.DISCONNECTED)
    override suspend fun calibrate(): Result<Unit> = executeCommand(DroneCommand.CALIBRATE)
    override suspend fun emergencyStop(): Result<Unit> = executeCommand(DroneCommand.EMERGENCY_STOP)

    private fun createApi(ipAddress: String, port: Int): DroneAPI {
        val client = OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(4, 2, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://$ipAddress:$port/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DroneAPI::class.java)
    }

    private fun requireApi(): DroneAPI = api ?: createApi(profile.ipAddress, profile.port).also { api = it }

    private fun DroneCommand.toEsp32Command(): String = when (this) {
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

    private fun Map<String, Any>.toTelemetry(
        battery: BatteryStatus?,
        gps: GPSData?
    ): Telemetry {
        return Telemetry(
            timestamp = number("timestamp", "time", default = System.currentTimeMillis().toDouble()).toLong(),
            altitude = number("altitude", "alt", default = gps?.altitude ?: lastTelemetry.altitude),
            speed = number("speed", "velocity", default = gps?.speed?.toDouble() ?: lastTelemetry.speed),
            temperature = number("temperature", "temp", "cpu_temp", default = battery?.temperature?.toDouble() ?: lastTelemetry.temperature),
            batteryVoltage = number("battery_voltage", "batteryVoltage", "voltage", default = battery?.voltage?.toDouble() ?: lastTelemetry.batteryVoltage),
            gpsSatellites = number("gps_satellites", "satellites", default = gps?.satellites?.toDouble() ?: lastTelemetry.gpsSatellites.toDouble()).toInt(),
            gpsSignalStrength = number("gps_signal", "gpsSignalStrength", default = gps?.accuracy?.let { (100 - it * 8).coerceIn(0f, 100f).toDouble() } ?: lastTelemetry.gpsSignalStrength.toDouble()).toInt(),
            pitch = number("pitch", default = lastTelemetry.pitch),
            roll = number("roll", default = lastTelemetry.roll),
            yaw = number("yaw", "heading", default = gps?.heading?.toDouble() ?: lastTelemetry.yaw),
            windSpeed = number("wind_speed", "windSpeed", default = lastTelemetry.windSpeed),
            humidity = number("humidity", default = lastTelemetry.humidity)
        )
    }

    private fun Map<String, Any>.number(vararg keys: String, default: Double): Double {
        val entry = entries.firstOrNull { item ->
            keys.any { key -> item.key.equals(key, ignoreCase = true) }
        } ?: return default
        return when (val value = entry.value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }
}
