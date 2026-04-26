package com.triplogger.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.triplogger.R
import com.triplogger.ui.viewmodels.TripViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class StatisticsFragment : Fragment() {
    private lateinit var viewModel: TripViewModel
    private lateinit var yearSpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var tripCountText: TextView
    private lateinit var totalDistanceText: TextView
    private lateinit var totalFuelText: TextView
    private lateinit var avgConsumptionText: TextView
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TripViewModel::class.java]
        
        yearSpinner = view.findViewById(R.id.yearSpinner)
        monthSpinner = view.findViewById(R.id.monthSpinner)
        tripCountText = view.findViewById(R.id.tripCountText)
        totalDistanceText = view.findViewById(R.id.totalDistanceText)
        totalFuelText = view.findViewById(R.id.totalFuelText)
        avgConsumptionText = view.findViewById(R.id.avgConsumptionText)
        
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 1).toList()
        yearSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearSpinner.setSelection(years.indexOf(currentYear))
        
        val months = listOf("Весь год") + (1..12).map { "$it месяц" }
        monthSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadStatistics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadStatistics()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        lifecycleScope.launch {
            viewModel.statistics.collectLatest { stats ->
                stats?.let {
                    tripCountText.text = "Количество поездок: ${it.tripCount}"
                    totalDistanceText.text = String.format("Общий пробег: %.2f км", it.totalDistance)
                    totalFuelText.text = String.format("Общий расход: %.2f л", it.totalFuel)
                    avgConsumptionText.text = String.format("Средний расход: %.2f л/100км", it.averageConsumption)
                }
            }
        }
    }
    
    private fun loadStatistics() {
        val year = yearSpinner.selectedItem as Int
        val monthPosition = monthSpinner.selectedItemPosition
        
        if (monthPosition == 0) {
            viewModel.loadStatistics(year)
        } else {
            viewModel.loadStatistics(year, monthPosition)
        }
    }
}
