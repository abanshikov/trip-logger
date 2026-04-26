package com.triplogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val startPoint: String,
    val endPoint: String,
    val odometerStart: Double,
    val odometerEnd: Double,
    val gpsDistanceKm: Double,
    val manualDistanceKm: Double,
    val usedGpsDistance: Boolean,
    val fuelConsumed: Double,
    val seasonNorm: Double,
    val comment: String
)
