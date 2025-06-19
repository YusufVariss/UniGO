package com.example.unigo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.databinding.ItemUniversityRecommendationBinding
import com.example.unigo.repository.UniversityRecommendation

class UniversityRecommendationAdapter(
    private val onItemClick: (UniversityRecommendation) -> Unit
) : RecyclerView.Adapter<UniversityRecommendationAdapter.ViewHolder>() {

    private var recommendations: List<UniversityRecommendation> = emptyList()

    fun updateRecommendations(newRecommendations: List<UniversityRecommendation>) {
        recommendations = newRecommendations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUniversityRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size

    class ViewHolder(
        private val binding: ItemUniversityRecommendationBinding,
        private val onItemClick: (UniversityRecommendation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recommendation: UniversityRecommendation) {
            // İlk eşleşen bölümü al (en yüksek eşleşme puanına sahip olan)
            val bestDepartment = recommendation.matchingDepartments.firstOrNull()

            binding.apply {
                universityNameText.text = recommendation.university.name
                departmentNameText.text = bestDepartment?.name ?: "Bölüm bilgisi yok"
                cityChip.text = recommendation.university.city
                typeChip.text = recommendation.university.type.toString()
                quotaChip.text = "${bestDepartment?.quota ?: 0} Kontenjan"
                lastYearScoreText.text = bestDepartment?.minScore?.toString() ?: "Bilgi yok"
                lastYearRankText.text = bestDepartment?.minRank?.toString() ?: "Bilgi yok"
                reasonText.text = "Eşleşme Puanı: %.2f".format(recommendation.matchScore)

                // Tıklama olayını ekle
                root.setOnClickListener {
                    onItemClick(recommendation)
                }
            }
        }
    }
}