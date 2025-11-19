package com.example.blm

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blm.databinding.ItemChecklistBinding
import com.example.blm.model.ChecklistItem

class ChecklistAdapter(private val items: MutableList<ChecklistItem>) :
    RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChecklistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Remove listener before setting text to avoid infinite loops
        holder.binding.etItemText.tag = null

        holder.binding.etItemText.setText(item.text)
        holder.binding.cbItem.isChecked = item.isChecked

        // Update data when text changes
        holder.binding.etItemText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                item.text = s.toString()
            }
        })

        // Update data when checkbox changes
        holder.binding.cbItem.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem() {
        items.add(ChecklistItem())
        notifyItemInserted(items.size - 1)
    }
}