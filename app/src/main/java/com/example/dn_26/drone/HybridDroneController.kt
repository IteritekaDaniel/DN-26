package com.example.dn_26.drone

import android.content.Context
import com.example.dn_26.domain.model.ConnectionMode
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Telemetry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class HybridDroneController(
    context: Context
) : IDroneController, ConfigurableDroneController {

    private val simulationController = FakeDroneController()
    private val wifiController = Esp32HttpDroneController()
    private val bluetoothController = Esp32BluetoothDroneController(context.applicationContext)
    private val activeController = MutableStateFlow<IDroneController>(simulationController)
    private var profile = ConnectionProfile()

    override suspend fun configureConnection(profile: ConnectionProfile): Result<Unit> = runCatching {
        activeController.value.disconnect()
        this.profile = profile
        val nextController = when (profile.mode) {
            ConnectionMode.SIMULATION -> simulationController
            ConnectionMode.ESP32_WIFI -> wifiController
            ConnectionMode.BLUETOOTH -> bluetoothController
        }

        if (nextController is ConfigurableDroneController) {
            nextController.configureConnection(profile).getOrThrow()
        }
        activeController.value = nextController
    }

    override fun currentConnectionProfile(): ConnectionProfile = profile

    override suspend fun initialize(): Result<Unit> = activeController.value.initialize()
    override suspend fun connect(): Result<Unit> = activeController.value.connect()
    override suspend fun disconnect(): Result<Unit> = activeController.value.disconnect()

    override suspend fun executeCommand(
        command: DroneCommand,
        parameters: Map<String, Any>
    ): Result<Unit> = activeController.value.executeCommand(command, parameters)

    override suspend fun updateJoystickInput(
        x: Float,
        y: Float,
        z: Float,
        rotation: Float
    ): Result<Unit> = activeController.value.updateJoystickInput(x, y, z, rotation)

    override fun observeTelemetry(): Flow<Telemetry> = activeController.flatMapLatest { it.observeTelemetry() }
    override fun observeState(): Flow<DroneState> = activeController.flatMapLatest { it.observeState() }
    override suspend fun getBatteryLevel(): Result<Int> = activeController.value.getBatteryLevel()
    override suspend fun isConnected(): Result<Boolean> = activeController.value.isConnected()
    override suspend fun calibrate(): Result<Unit> = activeController.value.calibrate()
    override suspend fun emergencyStop(): Result<Unit> = activeController.value.emergencyStop()
}
