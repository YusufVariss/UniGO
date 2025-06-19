package com.example.unigo.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.R

class BolumAdapter(private val bolumList: List<Bolum>) : RecyclerView.Adapter<BolumAdapter.BolumViewHolder>() {
    class BolumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val universiteText: TextView = itemView.findViewById(R.id.universiteText)
        val bolumText: TextView = itemView.findViewById(R.id.bolumText)
        val sehirText: TextView = itemView.findViewById(R.id.sehirText)
        val puanTuruText: TextView = itemView.findViewById(R.id.puanTuruText)
        val tabanPuanText: TextView = itemView.findViewById(R.id.tabanPuanText)
        val kontenjanText: TextView = itemView.findViewById(R.id.kontenjanText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BolumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bolum, parent, false)
        return BolumViewHolder(view)
    }

    override fun onBindViewHolder(holder: BolumViewHolder, position: Int) {
        val bolum = bolumList[position]
        holder.universiteText.text = bolum.universite
        holder.bolumText.text = bolum.bolum
        holder.sehirText.text = bolum.sehir
        holder.puanTuruText.text = "Puan Türü: ${bolum.puan_turu}"
        holder.tabanPuanText.text = "Taban Puan: %.5f".format(bolum.taban_puan)
        holder.kontenjanText.text = "Kontenjan: ${bolum.kontenjan}"
    }

    override fun getItemCount(): Int = bolumList.size
}