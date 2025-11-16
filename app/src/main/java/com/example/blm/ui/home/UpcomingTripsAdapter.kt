package com.example.blm.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blm.R
import com.example.blm.model.Trip

class UpcomingTripsAdapter(
    private val trips: List<Trip>
) : RecyclerView.Adapter<UpcomingTripsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tripTitle)
        val date: TextView = view.findViewById(R.id.tripDate)
        val group: TextView = view.findViewById(R.id.tripGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = trips[position]

        holder.title.text = trip.title
        holder.date.text = trip.date

        if (trip.isSolo) {
            holder.group.text = "Private (Solo Trip)"
        } else {
            holder.group.text = trip.group?.name ?: "Group"
        }

        // TODO: Set an onClickListener to navigate to the trip details
        // holder.itemView.setOnClickListener { ... }
    }

    override fun getItemCount() = trips.size
}