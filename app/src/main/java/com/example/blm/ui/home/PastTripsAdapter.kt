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

class PastTripsAdapter(
    private val memories: List<Trip>
) : RecyclerView.Adapter<PastTripsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.memoryTitle)
        val date: TextView = view.findViewById(R.id.memoryDate)
        val image: ImageView = view.findViewById(R.id.memoryImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memory = memories[position]

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

        // TODO: Set an onClickListener
        // holder.itemView.setOnClickListener { ... }
    }

    override fun getItemCount() = memories.size
}