package com.example.unigo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.databinding.ItemUniversityBinding
import com.example.unigo.models.University
import com.example.unigo.models.UniversityType

class UniversiteOnerileriAdapter(
    private val onItemClick: (University) -> Unit
) : ListAdapter<University, UniversiteOnerileriAdapter.UniversityViewHolder>(UniversityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityViewHolder {
        val binding = ItemUniversityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UniversityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UniversityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<University>) {
        submitList(newList)
    }

    inner class UniversityViewHolder(
        private val binding: ItemUniversityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(university: University) {
            binding.universityName.text = university.name
            binding.universityType.text = when (university.type) {
                UniversityType.DEVLET -> "Devlet"
                UniversityType.VAKIF -> "Vakıf"
            }
        }
    }

    private class UniversityDiffCallback : DiffUtil.ItemCallback<University>() {
        override fun areItemsTheSame(oldItem: University, newItem: University): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: University, newItem: University): Boolean {
            return oldItem == newItem
        }
    }
}