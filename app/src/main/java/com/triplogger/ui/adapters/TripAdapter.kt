package com.triplogger.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.triplogger.R
import com.triplogger.data.TripEntity

class TripAdapter(
    private val onDeleteClick: (TripEntity) -> Unit,
    private val onItemClick: (TripEntity) -> Unit
) : ListAdapter<TripEntity, TripAdapter.ViewHolder>(TripDiffCallback()) {

    class ViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val routeText: TextView = view.findViewById(R.id.routeText)
        val distanceText: TextView = view.findViewById(R.id.distanceText)
        val fuelText: TextView = view.findViewById(R.id.fuelText)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = getItem(position)
        val totalDistance = if (trip.usedGpsDistance) trip.gpsDistanceKm else trip.manualDistanceKm

        holder.dateText.text = trip.date
        holder.timeText.text = "${trip.departureTime} - ${trip.arrivalTime}"
        holder.routeText.text = "${trip.startPoint} → ${trip.endPoint}"
        holder.distanceText.text = String.format("Пробег: %.0f км", totalDistance)
        holder.fuelText.text = String.format("Расход: %.2f л (норма %.2f)", trip.fuelConsumed, trip.seasonNorm)

        holder.itemView.setOnClickListener { onItemClick(trip) }
        holder.deleteButton.setOnClickListener { onDeleteClick(trip) }
    }

    class TripDiffCallback : DiffUtil.ItemCallback<TripEntity>() {
        override fun areItemsTheSame(oldItem: TripEntity, newItem: TripEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TripEntity, newItem: TripEntity) = oldItem == newItem
    }
}
