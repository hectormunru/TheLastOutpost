package com.example.thelastoutpost.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thelastoutpost.databinding.ItemSaveSlotBinding
import com.example.thelastoutpost.model.SaveSlot

class SaveSlotAdapter(
    private val onSlotClick: (SaveSlot) -> Unit,
    private val onDeleteClick: (SaveSlot) -> Unit
) : ListAdapter<SaveSlot, SaveSlotAdapter.SlotViewHolder>(SaveSlotDiffCallback()) {

    class SlotViewHolder(private val binding: ItemSaveSlotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(slot: SaveSlot, onSlotClick: (SaveSlot) -> Unit, onDeleteClick: (SaveSlot) -> Unit) {
            binding.slot = slot
            binding.root.setOnClickListener { onSlotClick(slot) }
            binding.btnDelete.setOnClickListener { onDeleteClick(slot) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val binding = ItemSaveSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(getItem(position), onSlotClick, onDeleteClick)
    }
}

class SaveSlotDiffCallback : DiffUtil.ItemCallback<SaveSlot>() {
    override fun areItemsTheSame(oldItem: SaveSlot, newItem: SaveSlot): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SaveSlot, newItem: SaveSlot): Boolean {
        return oldItem == newItem
    }
}
