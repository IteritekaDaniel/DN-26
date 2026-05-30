package com.example.dn_26.drone

import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Telemetry
import kotlinx.coroutines.flow.Flow

interface IDroneController {
    suspend fun initialize(): Result<Unit>
    suspend fun connect(): Result<Unit>
    suspend fun disconnect(): Result<Unit>
    suspend fun executeCommand(command: DroneCommand, parameters: Map<String, Any> = emptyMap()): Result<Unit>
    suspend fun updateJoystickInput(x: Float, y: Float, z: Float, rotation: Float): Result<Unit>
    fun observeTelemetry(): Flow<Telemetry>
    fun observeState(): Flow<DroneState>
    suspend fun getBatteryLevel(): Result<Int>
    suspend fun isConnected(): Result<Boolean>
    suspend fun calibrate(): Result<Unit>
    suspend fun emergencyStop(): Result<Unit>
}