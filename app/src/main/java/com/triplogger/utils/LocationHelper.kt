package com.triplogger.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val activity: Activity) {
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private var locationCallback: LocationCallback? = null
    private var startLocation: Location? = null
    private var endLocation: Location? = null
    private var onLocationResult: ((Location, Boolean) -> Unit)? = null
    
    fun requestCurrentLocation(isStart: Boolean, callback: (Location, Boolean) -> Unit) {
        onLocationResult = callback
        
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        
        val locationRequest = LocationRequest.Builder(10000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    if (isStart) startLocation = location else endLocation = location
                    onLocationResult?.invoke(location, isStart)
                    stopLocationUpdates()
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }
    
    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }
    
    fun getDistanceBetween(): Double {
        return if (startLocation != null && endLocation != null) {
            startLocation!!.distanceTo(endLocation!!) / 1000.0
        } else 0.0
    }
    
    fun resetLocations() {
        startLocation = null
        endLocation = null
    }
}
