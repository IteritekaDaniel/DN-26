package com.example.dn_26.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: TelemetryEntity)

    @Query("SELECT * FROM telemetry_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTelemetry(limit: Int): Flow<List<TelemetryEntity>>

    @Query("SELECT * FROM telemetry_history WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTelemetryInRange(startTime: Long, endTime: Long): List<TelemetryEntity>

    @Query("DELETE FROM telemetry_history")
    suspend fun deleteAllTelemetry()
}

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("UPDATE alerts SET is_read = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: String)

    @Query("DELETE FROM alerts")
    suspend fun deleteAllAlerts()
}