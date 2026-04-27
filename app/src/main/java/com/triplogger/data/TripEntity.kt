package com.triplogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val departureTime: String,
    val arrivalTime: String,
    val startPoint: String,
    val endPoint: String,
    val odometerStart: Int,
    val odometerEnd: Int,
    val gpsDistanceKm: Double,
    val manualDistanceKm: Double,
    val usedGpsDistance: Boolean,
    val fuelConsumed: Double,
    val seasonNorm: Double,
    val comment: String
)
