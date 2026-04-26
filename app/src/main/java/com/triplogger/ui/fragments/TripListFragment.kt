package com.triplogger.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.triplogger.R
import com.triplogger.data.TripEntity
import com.triplogger.ui.adapters.TripAdapter
import com.triplogger.ui.viewmodels.TripViewModel
import com.triplogger.utils.ExcelExporter
import com.triplogger.utils.PdfExporter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripListFragment : Fragment() {
    private lateinit var viewModel: TripViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private var currentFilter: Pair<String, String>? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TripViewModel::class.java]
        
        recyclerView = view.findViewById(R.id.recyclerView)
        
        adapter = TripAdapter(
            onDeleteClick = { trip -> viewModel.deleteTrip(trip) },
            onItemClick = { trip -> }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        view.findViewById<Button>(R.id.filterButton).setOnClickListener { showDateRangePicker() }
        view.findViewById<Button>(R.id.exportExcelButton).setOnClickListener { exportToExcel() }
        view.findViewById<Button>(R.id.exportPdfButton).setOnClickListener { exportToPdf() }
        
        lifecycleScope.launch {
            viewModel.allTrips.collectLatest { trips ->
                adapter.submitList(trips)
            }
        }
    }
    
    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите период")
            .build()
        
        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(selection.first))
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(selection.second))
            currentFilter = startDate to endDate
            
            lifecycleScope.launch {
                viewModel.getTripsBetweenDates(startDate, endDate).collectLatest { trips ->
                    adapter.submitList(trips)
                }
            }
        }
        
        dateRangePicker.show(parentFragmentManager, "date_range_picker")
    }
    
    private fun exportToExcel() {
        lifecycleScope.launch {
            val trips = if (currentFilter != null) {
                viewModel.allTrips.value.filter { trip ->
                    trip.date >= currentFilter!!.first && trip.date <= currentFilter!!.second
                }
            } else {
                viewModel.allTrips.value
            }
            
            val uri = ExcelExporter(requireContext()).exportTrips(trips)
            Toast.makeText(requireContext(), "Excel файл сохранен", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun exportToPdf() {
        lifecycleScope.launch {
            val trips = if (currentFilter != null) {
                viewModel.allTrips.value.filter { trip ->
                    trip.date >= currentFilter!!.first && trip.date <= currentFilter!!.second
                }
            } else {
                viewModel.allTrips.value
            }
            
            val stats = viewModel.statistics.value
            
            val uri = PdfExporter(requireContext()).exportTrips(trips, stats)
            Toast.makeText(requireContext(), "PDF файл сохранен", Toast.LENGTH_SHORT).show()
        }
    }
}
