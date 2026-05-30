package com.example.dn_26.drone

import com.example.dn_26.domain.model.ConnectionProfile

interface ConfigurableDroneController {
    suspend fun configureConnection(profile: ConnectionProfile): Result<Unit>
    fun currentConnectionProfile(): ConnectionProfile
}
