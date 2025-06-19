package com.example.unigo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.R
import com.example.unigo.models.Program

class ProgramAdapter(
    private var programs: List<Program>
) : RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder>() {

    class ProgramViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.programNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_program, parent, false)
        return ProgramViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        val program = programs[position]
        holder.nameTextView.text = program.name
        holder.itemView.isClickable = false
        holder.itemView.isFocusable = false
    }

    override fun getItemCount() = programs.size

    fun updateData(newPrograms: List<Program>) {
        programs = newPrograms
        notifyDataSetChanged()
    }
}