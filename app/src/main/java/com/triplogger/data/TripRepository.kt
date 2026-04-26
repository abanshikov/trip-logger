package com.triplogger.data

import kotlinx.coroutines.flow.Flow

class TripRepository(private val dao: TripDao) {
    fun getAllTrips(): Flow<List<TripEntity>> = dao.getAllTrips()
    
    fun getTripsBetweenDates(start: String, end: String): Flow<List<TripEntity>> = 
        dao.getTripsBetweenDates(start, end)
    
    suspend fun getLastTrip(): TripEntity? = dao.getLastTrip()
    
    suspend fun getStatistics(start: String, end: String): TripStatistics {
        val totalManualDistance = dao.getTotalManualDistanceBetween(start, end) ?: 0.0
        val totalGpsDistance = dao.getTotalGpsDistanceBetween(start, end) ?: 0.0
        val totalFuel = dao.getTotalFuelBetween(start, end) ?: 0.0
        val tripCount = dao.getTripCountBetween(start, end) ?: 0
        
        return TripStatistics(
            tripCount = tripCount,
            totalDistance = totalManualDistance + totalGpsDistance,
            totalFuel = totalFuel,
            averageConsumption = if (totalManualDistance + totalGpsDistance > 0) 
                (totalFuel * 100) / (totalManualDistance + totalGpsDistance) else 0.0
        )
    }
    
    suspend fun insert(trip: TripEntity): Long = dao.insert(trip)
    suspend fun delete(trip: TripEntity) = dao.delete(trip)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}

data class TripStatistics(
    val tripCount: Int,
    val totalDistance: Double,
    val totalFuel: Double,
    val averageConsumption: Double
)
