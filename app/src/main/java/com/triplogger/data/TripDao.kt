package com.triplogger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY date DESC, id DESC")
    fun getAllTrips(): Flow<List<TripEntity>>
    
    @Query("SELECT * FROM trips WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTripsBetweenDates(startDate: String, endDate: String): Flow<List<TripEntity>>
    
    @Query("SELECT SUM(manualDistanceKm) FROM trips WHERE usedGpsDistance = 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalManualDistanceBetween(startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(gpsDistanceKm) FROM trips WHERE usedGpsDistance = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalGpsDistanceBetween(startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(fuelConsumed) FROM trips WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalFuelBetween(startDate: String, endDate: String): Double?
    
    @Query("SELECT COUNT(*) FROM trips WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTripCountBetween(startDate: String, endDate: String): Int?
    
    @Insert
    suspend fun insert(trip: TripEntity): Long
    
    @Update
    suspend fun update(trip: TripEntity)
    
    @Delete
    suspend fun delete(trip: TripEntity)
    
    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteById(tripId: Long)
}
