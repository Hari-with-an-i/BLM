package com.example.blm.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blm.R
import com.example.blm.model.Trip
// TODO: Import an image loading library like Glide or Coil
// import com.bumptech.glide.Glide

//
// --- 1. THIS IS THE "CLASS CONSTRUCTOR" ---
//
// We are modifying it to accept a "click listener" function
// that we will pass in from MainActivity.
//
class PastTripsAdapter(
    private val memories: List<Trip>,
    private val onTripClick: (Trip) -> Unit // This is the new part
) : RecyclerView.Adapter<PastTripsAdapter.ViewHolder>() {

    // This ViewHolder class is unchanged.
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.memoryTitle)
        val date: TextView = view.findViewById(R.id.memoryDate)
        val image: ImageView = view.findViewById(R.id.memoryImage)
    }

    // This function is unchanged.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory_card, parent, false)
        return ViewHolder(view)
    }

    //
    // --- 2. THIS IS THE "onBindViewHolder" FUNCTION ---
    //
    // We are modifying it to set a click listener on the item.
    //
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the specific memory for this row
        val memory = memories[position]

        // --- This part is the same as before ---
        holder.title.text = memory.title
        holder.date.text = memory.date

        // TODO: Use an image loading library to load the image
        // if (memory.imageUrl != null) {
        //    Glide.with(holder.itemView.context)
        //        .load(memory.imageUrl)
        //        .into(holder.image)
        // } else {
        //    holder.image.setImageResource(R.drawable.ic_launcher_background) // A default
        // }
        // --- End of same part ---


        // --- THIS IS THE NEW PART ---
        // We set a click listener on the entire card view
        holder.itemView.setOnClickListener {
            // When clicked, call the "onTripClick" function
            // that was passed into our constructor.
            onTripClick(memory)
        }
    }

    // This function is unchanged.
    override fun getItemCount() = memories.size
}