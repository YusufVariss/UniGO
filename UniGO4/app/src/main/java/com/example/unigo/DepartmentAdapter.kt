package com.example.unigo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.databinding.ItemDepartmentBinding
import com.example.unigo.models.Department
import com.example.unigo.models.DepartmentType

class DepartmentAdapter(
    private val onItemClick: ((Department) -> Unit)? = null
) : ListAdapter<Department, DepartmentAdapter.DepartmentViewHolder>(DepartmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val binding = ItemDepartmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DepartmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<Department>) {
        submitList(newList)
    }

    inner class DepartmentViewHolder(
        private val binding: ItemDepartmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(department: Department) {
            binding.apply {
                departmentNameText.text = department.name
                departmentTypeText.text = when (department.type) {
                    DepartmentType.TWO_YEAR -> "2 Yıllık"
                    DepartmentType.FOUR_YEAR -> "4 Yıllık"
                    DepartmentType.OPEN_EDUCATION -> TODO()
                }
                if (department.isOpenEducation) {
                    openEducationTextView.visibility = ViewGroup.VISIBLE
                    openEducationTextView.text = "Açık Öğretim"
                } else {
                    openEducationTextView.visibility = ViewGroup.GONE
                }
            }
        }
    }

    private class DepartmentDiffCallback : DiffUtil.ItemCallback<Department>() {
        override fun areItemsTheSame(oldItem: Department, newItem: Department): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Department, newItem: Department): Boolean {
            return oldItem == newItem
        }
    }
}