package com.example.blm.ui.polls

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blm.R
import com.example.blm.model.Poll
import com.example.blm.model.PollOption

class PollAdapter(
    private val onVote: (Poll, PollOption) -> Unit,
    private val onDelete: (Poll) -> Unit
) : RecyclerView.Adapter<PollAdapter.PollVH>() {

    private val items = mutableListOf<Poll>()

    fun submitList(newList: List<Poll>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poll, parent, false)
        return PollVH(v)
    }

    override fun onBindViewHolder(holder: PollVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PollVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTitle = view.findViewById<TextView>(R.id.tvPollTitle)
        private val deleteBtn = view.findViewById<ImageView>(R.id.ivDeletePoll)
        private val layoutOptions = view.findViewById<LinearLayout>(R.id.layoutOptions)

        fun bind(poll: Poll) {
            tvTitle.text = poll.title

            // DELETE POLL
            deleteBtn.setOnClickListener {
                onDelete(poll)
            }

            // OPTIONS
            layoutOptions.removeAllViews()
            for (opt in poll.options) {
                val option = opt
                val btn = Button(itemView.context).apply {
                    text = "${option.text} (${option.votes})"
                    setOnClickListener { onVote(poll, option) }
                }
                layoutOptions.addView(btn)
            }
        }
    }
}
