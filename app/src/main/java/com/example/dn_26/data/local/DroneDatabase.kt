package com.example.dn_26.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TelemetryEntity::class, AlertEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DroneDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: DroneDatabase? = null

        fun getDatabase(context: Context): DroneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DroneDatabase::class.java,
                    "drone_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}