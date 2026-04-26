package com.triplogger.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trip_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val KEY_SUMMER_NORM = "summer_norm"
        const val KEY_WINTER_NORM = "winter_norm"
        const val KEY_WINTER_START_MONTH = "winter_start_month"
        const val KEY_WINTER_END_MONTH = "winter_end_month"
        
        const val DEFAULT_SUMMER_NORM = 10.5f
        const val DEFAULT_WINTER_NORM = 11.5f
        const val DEFAULT_WINTER_START = 11
        const val DEFAULT_WINTER_END = 3
    }
    
    var summerNorm: Float
        get() = prefs.getFloat(KEY_SUMMER_NORM, DEFAULT_SUMMER_NORM)
        set(value) = prefs.edit().putFloat(KEY_SUMMER_NORM, value).apply()
    
    var winterNorm: Float
        get() = prefs.getFloat(KEY_WINTER_NORM, DEFAULT_WINTER_NORM)
        set(value) = prefs.edit().putFloat(KEY_WINTER_NORM, value).apply()
    
    var winterStartMonth: Int
        get() = prefs.getInt(KEY_WINTER_START_MONTH, DEFAULT_WINTER_START)
        set(value) = prefs.edit().putInt(KEY_WINTER_START_MONTH, value).apply()
    
    var winterEndMonth: Int
        get() = prefs.getInt(KEY_WINTER_END_MONTH, DEFAULT_WINTER_END)
        set(value) = prefs.edit().putInt(KEY_WINTER_END_MONTH, value).apply()
    
    fun getCurrentSeasonNorm(): Float {
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        return when {
            winterStartMonth <= winterEndMonth -> {
                if (currentMonth in winterStartMonth..winterEndMonth) winterNorm else summerNorm
            }
            else -> {
                if (currentMonth >= winterStartMonth || currentMonth <= winterEndMonth) winterNorm else summerNorm
            }
        }
    }
}
