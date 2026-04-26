package com.triplogger.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.triplogger.TripLoggerApplication
import com.triplogger.data.*
import com.triplogger.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TripLoggerApplication).repository
    private val prefs = PreferencesManager(application)
    
    val allTrips: StateFlow<List<TripEntity>> = repository.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _statistics = MutableStateFlow<TripStatistics?>(null)
    val statistics: StateFlow<TripStatistics?> = _statistics.asStateFlow()
    
    private val _lastTrip = MutableStateFlow<TripEntity?>(null)
    val lastTrip: StateFlow<TripEntity?> = _lastTrip.asStateFlow()
    
    init {
        loadLastTrip()
    }
    
    fun loadLastTrip() {
        viewModelScope.launch {
            _lastTrip.value = repository.getLastTrip()
        }
    }
    
    fun loadStatistics(year: Int, month: Int? = null) {
        viewModelScope.launch {
            val (startDate, endDate) = if (month != null) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val start = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                start to end
            } else {
                "$year-01-01" to "$year-12-31"
            }
            _statistics.value = repository.getStatistics(startDate, endDate)
        }
    }
    
    fun saveTrip(
        date: String,
        startPoint: String,
        endPoint: String,
        odometerStart: Double,
        odometerEnd: Double,
        gpsDistance: Double,
        manualDistance: Double,
        usedGps: Boolean,
        comment: String
    ) {
        viewModelScope.launch {
            val seasonNorm = prefs.getCurrentSeasonNorm().toDouble()
            val finalDistance = if (usedGps) gpsDistance else manualDistance
            val fuelConsumed = (finalDistance * seasonNorm) / 100.0
            
            val trip = TripEntity(
                date = date,
                startPoint = startPoint,
                endPoint = endPoint,
                odometerStart = odometerStart,
                odometerEnd = odometerEnd,
                gpsDistanceKm = gpsDistance,
                manualDistanceKm = manualDistance,
                usedGpsDistance = usedGps,
                fuelConsumed = fuelConsumed,
                seasonNorm = seasonNorm,
                comment = comment
            )
            repository.insert(trip)
            loadLastTrip() // Обновляем последнюю поездку после сохранения
        }
    }
    
    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.delete(trip)
            loadLastTrip() // Обновляем после удаления
        }
    }
    
    fun getTripsBetweenDates(start: String, end: String): Flow<List<TripEntity>> {
        return repository.getTripsBetweenDates(start, end)
    }
    
    suspend fun getStatisticsForPeriod(start: String, end: String): TripStatistics {
        return repository.getStatistics(start, end)
    }
}
