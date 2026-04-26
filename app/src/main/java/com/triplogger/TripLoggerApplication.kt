package com.triplogger

import android.app.Application
import com.triplogger.data.AppDatabase

class TripLoggerApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { com.triplogger.data.TripRepository(database.tripDao()) }
}
