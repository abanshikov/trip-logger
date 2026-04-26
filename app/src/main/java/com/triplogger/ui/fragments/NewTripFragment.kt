package com.triplogger.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.triplogger.R
import com.triplogger.ui.viewmodels.TripViewModel
import com.triplogger.utils.LocationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NewTripFragment : Fragment() {
    private lateinit var viewModel: TripViewModel
    private lateinit var locationHelper: LocationHelper
    
    private lateinit var dateInput: TextInputEditText
    private lateinit var startPointInput: TextInputEditText
    private lateinit var endPointInput: TextInputEditText
    private lateinit var odometerStartInput: TextInputEditText
    private lateinit var odometerEndInput: TextInputEditText
    private lateinit var gpsDistanceText: TextView
    private lateinit var manualDistanceText: TextView
    private lateinit var useGpsSwitch: SwitchMaterial
    private lateinit var fuelConsumedText: TextView
    private lateinit var commentInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    
    private var gpsDistance = 0.0
    private var manualDistance = 0.0
    private var startLocationReceived = false
    private var endLocationReceived = false
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_trip, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TripViewModel::class.java]
        locationHelper = LocationHelper(requireActivity())
        
        dateInput = view.findViewById(R.id.dateInput)
        startPointInput = view.findViewById(R.id.startPointInput)
        endPointInput = view.findViewById(R.id.endPointInput)
        odometerStartInput = view.findViewById(R.id.odometerStartInput)
        odometerEndInput = view.findViewById(R.id.odometerEndInput)
        gpsDistanceText = view.findViewById(R.id.gpsDistanceText)
        manualDistanceText = view.findViewById(R.id.manualDistanceText)
        useGpsSwitch = view.findViewById(R.id.useGpsSwitch)
        fuelConsumedText = view.findViewById(R.id.fuelConsumedText)
        commentInput = view.findViewById(R.id.commentInput)
        saveButton = view.findViewById(R.id.saveButton)
        
        dateInput.setText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
        
        loadLastTripData()
        
        view.findViewById<Button>(R.id.getStartLocation).setOnClickListener { getLocation(true) }
        view.findViewById<Button>(R.id.getEndLocation).setOnClickListener { getLocation(false) }
        
        odometerStartInput.addTextChangedListener { recalculateDistance() }
        odometerEndInput.addTextChangedListener { recalculateDistance() }
        useGpsSwitch.setOnCheckedChangeListener { _, _ -> recalculateFuel() }
        saveButton.setOnClickListener { saveTrip() }
    }
    
    private fun loadLastTripData() {
        lifecycleScope.launch {
            viewModel.lastTrip.collect { lastTrip ->
                lastTrip?.let {
                    if (startPointInput.text.isNullOrEmpty()) {
                        startPointInput.setText(it.endPoint)
                    }
                    
                    if (odometerStartInput.text.isNullOrEmpty()) {
                        odometerStartInput.setText(String.format("%.1f", it.odometerEnd))
                    }
                    
                    if (it.endPoint.isNotEmpty() || it.odometerEnd > 0) {
                        Toast.makeText(requireContext(), 
                            "Заполнено из последней поездки от " + it.date, 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun getLocation(isStart: Boolean) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        
        locationHelper.requestCurrentLocation(isStart) { location, isStartLoc ->
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "${location.latitude}, ${location.longitude}"
            
            if (isStartLoc) {
                startPointInput.setText(address)
                startLocationReceived = true
            } else {
                endPointInput.setText(address)
                endLocationReceived = true
            }
            
            if (startLocationReceived && endLocationReceived) {
                gpsDistance = locationHelper.getDistanceBetween()
                gpsDistanceText.text = String.format("%.2f км", gpsDistance)
            }
            
            recalculateFuel()
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadLastTrip()
    }
    
    private fun recalculateDistance() {
        val start = odometerStartInput.text.toString().toDoubleOrNull()
        val end = odometerEndInput.text.toString().toDoubleOrNull()
        
        if (start != null && end != null && end > start) {
            manualDistance = end - start
            manualDistanceText.text = String.format("%.2f км", manualDistance)
            recalculateFuel()
        }
    }
    
    private fun recalculateFuel() {
        val distance = if (useGpsSwitch.isChecked) gpsDistance else manualDistance
        val seasonNorm = com.triplogger.utils.PreferencesManager(requireContext()).getCurrentSeasonNorm().toDouble()
        val fuel = (distance * seasonNorm) / 100.0
        fuelConsumedText.text = String.format("%.2f л (норма %.2f л/100км)", fuel, seasonNorm)
    }
    
    private fun saveTrip() {
        val date = dateInput.text.toString()
        val startPoint = startPointInput.text.toString()
        val endPoint = endPointInput.text.toString()
        val odometerStart = odometerStartInput.text.toString().toDoubleOrNull() ?: 0.0
        val odometerEnd = odometerEndInput.text.toString().toDoubleOrNull() ?: 0.0
        val comment = commentInput.text.toString()
        val usedGps = useGpsSwitch.isChecked
        
        if (date.isEmpty() || (!usedGps && (odometerStart <= 0 || odometerEnd <= 0))) {
            Toast.makeText(requireContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.saveTrip(date, startPoint, endPoint, odometerStart, odometerEnd, gpsDistance, manualDistance, usedGps, comment)
        Toast.makeText(requireContext(), "Поездка сохранена", Toast.LENGTH_SHORT).show()
        
        startPointInput.text?.clear()
        endPointInput.text?.clear()
        odometerStartInput.text?.clear()
        odometerEndInput.text?.clear()
        commentInput.text?.clear()
        gpsDistance = 0.0
        manualDistance = 0.0
        gpsDistanceText.text = ""
        manualDistanceText.text = ""
        fuelConsumedText.text = ""
        locationHelper.resetLocations()
        
        viewModel.loadLastTrip()
    }
}
