package com.example.unigo.repository

import android.content.Context
import android.util.Log
import com.example.unigo.models.Department
import com.example.unigo.models.DepartmentType
import com.example.unigo.models.ExamType
import com.example.unigo.models.Score
import com.example.unigo.models.University
import com.example.unigo.models.UniversityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UniversityRecommendation(
    val university: University,
    val matchingDepartments: List<Department>,
    val matchScore: Double
)

class ScoreBasedRecommendationRepository(private val context: Context) {
    private val universityRepository = UniversityRepository(context)

    suspend fun getRecommendedUniversities(
        score: Score,
        preferredCities: List<String> = emptyList(),
        preferredTypes: List<UniversityType> = emptyList(),
        preferredDepartmentTypes: List<DepartmentType> = emptyList()
    ): List<UniversityRecommendation> = withContext(Dispatchers.Default) {
        Log.d("ScoreBasedRecommendation", "Öneri arama başladı:")
        Log.d("ScoreBasedRecommendation", "- Sınav tipi: ${score.examType}")
        Log.d("ScoreBasedRecommendation", "- TYT puanı: ${score.tytScore}")
        Log.d("ScoreBasedRecommendation", "- AYT puanı: ${score.aytScore}")
        Log.d("ScoreBasedRecommendation", "- YDT puanı: ${score.ydtScore}")
        Log.d("ScoreBasedRecommendation", "- Min sıralama: ${score.minRank}")
        Log.d("ScoreBasedRecommendation", "- Tercih edilen şehirler: $preferredCities")
        Log.d("ScoreBasedRecommendation", "- Tercih edilen üniversite tipleri: $preferredTypes")
        Log.d("ScoreBasedRecommendation", "- Tercih edilen bölüm tipleri: $preferredDepartmentTypes")

        // Veri yükleme kontrolü
        if (!universityRepository.isDataLoaded()) {
            Log.e("ScoreBasedRecommendation", "Üniversite verileri henüz yüklenmedi!")
            return@withContext emptyList()
        }

        val universities = universityRepository.getAllUniversities()
        if (universities.isEmpty()) {
            Log.e("ScoreBasedRecommendation", "Hiç üniversite verisi bulunamadı!")
            return@withContext emptyList()
        }
        Log.d("ScoreBasedRecommendation", "Toplam üniversite sayısı: ${universities.size}")

        val recommendations = mutableListOf<UniversityRecommendation>()
        var filteredByCity = 0
        var filteredByType = 0
        var filteredByDepartment = 0
        var filteredByScore = 0
        var filteredByRank = 0
        var matchedCount = 0

        universities.forEach { university ->
            // Şehir filtresi
            if (preferredCities.isNotEmpty() && university.city !in preferredCities) {
                filteredByCity++
                return@forEach
            }

            // Üniversite tipi filtresi
            if (preferredTypes.isNotEmpty() && university.type !in preferredTypes) {
                filteredByType++
                return@forEach
            }

            // Bölümleri filtrele
            val matchingDepartments = university.departments.filter { department ->
                // Bölüm tipi filtresi
                if (preferredDepartmentTypes.isNotEmpty() && department.type !in preferredDepartmentTypes) {
                    filteredByDepartment++
                    return@filter false
                }

                // Puan ve sıralama kontrolü (daha esnek)
                val minScore = department.minScore
                val maxScore = department.maxScore
                val minRank = department.minRank
                val maxRank = department.maxRank

                // Puan kontrolü
                val scoreMatch = when (score.examType) {
                    ExamType.TYT -> {
                        val studentScore = score.tytScore
                        val tolerance = 30.0 // Toleransı 30 puana çıkardık
                        if (minScore == null || maxScore == null) {
                            Log.d("ScoreBasedRecommendation", "Bölüm ${department.name} için puan bilgisi eksik")
                            false
                        } else {
                            studentScore >= minScore - tolerance && studentScore <= maxScore + tolerance
                        }
                    }
                    ExamType.SAY, ExamType.EA, ExamType.SOZ -> {
                        val studentScore = score.getTotalScore()
                        val tolerance = 30.0
                        if (minScore == null || maxScore == null) {
                            Log.d("ScoreBasedRecommendation", "Bölüm ${department.name} için puan bilgisi eksik")
                            false
                        } else {
                            studentScore >= minScore - tolerance && studentScore <= maxScore + tolerance
                        }
                    }
                    ExamType.DIL -> {
                        val studentScore = score.ydtScore
                        val tolerance = 30.0
                        if (minScore == null || maxScore == null) {
                            Log.d("ScoreBasedRecommendation", "Bölüm ${department.name} için puan bilgisi eksik")
                            false
                        } else {
                            studentScore >= minScore - tolerance && studentScore <= maxScore + tolerance
                        }
                    }
                }

                if (!scoreMatch) {
                    filteredByScore++
                    return@filter false
                }

                // Sıralama kontrolü
                val rankMatch = if (score.minRank != null) {
                    val tolerance = 15000 // Sıralama toleransını 15000'e çıkardık
                    if (minRank == null || maxRank == null) {
                        Log.d("ScoreBasedRecommendation", "Bölüm ${department.name} için sıralama bilgisi eksik")
                        true // Sıralama bilgisi yoksa bu kriteri atla
                    } else {
                        score.minRank <= minRank + tolerance && score.minRank >= maxRank - tolerance
                    }
                } else true

                if (!rankMatch) {
                    filteredByRank++
                    return@filter false
                }

                true
            }

            if (matchingDepartments.isNotEmpty()) {
                matchedCount++
                recommendations.add(
                    UniversityRecommendation(
                        university = university,
                        matchingDepartments = matchingDepartments,
                        matchScore = calculateMatchScore(score, matchingDepartments)
                    )
                )
            }
        }

        Log.d("ScoreBasedRecommendation", "Filtreleme sonuçları:")
        Log.d("ScoreBasedRecommendation", "- Şehir filtresine takılan: $filteredByCity")
        Log.d("ScoreBasedRecommendation", "- Üniversite tipi filtresine takılan: $filteredByType")
        Log.d("ScoreBasedRecommendation", "- Bölüm tipi filtresine takılan: $filteredByDepartment")
        Log.d("ScoreBasedRecommendation", "- Puan filtresine takılan: $filteredByScore")
        Log.d("ScoreBasedRecommendation", "- Sıralama filtresine takılan: $filteredByRank")
        Log.d("ScoreBasedRecommendation", "- Eşleşen üniversite sayısı: $matchedCount")
        Log.d("ScoreBasedRecommendation", "- Toplam öneri sayısı: ${recommendations.size}")

        // Eşleşme puanına göre sırala
        recommendations.sortedByDescending { it.matchScore }
    }

    private fun calculateMatchScore(score: Score, departments: List<Department>): Double {
        var totalScore = 0.0
        departments.forEach { department ->
            val studentScore = score.getTotalScore()

            // Puan farkını hesapla (fark ne kadar azsa o kadar iyi)
            val minScore = department.minScore ?: return@forEach
            val maxScore = department.maxScore ?: return@forEach
            val scoreDiff = Math.abs(maxScore - studentScore)
            val maxPossibleDiff = 500.0 // Maksimum olası puan farkı
            val scoreMatch = 1.0 - (scoreDiff / maxPossibleDiff)

            // Sıralama farkını hesapla
            val minRank = department.minRank ?: return@forEach
            val maxRank = department.maxRank ?: return@forEach
            val rankDiff = Math.abs(maxRank - score.minRank)
            val maxPossibleRankDiff = 1000000 // Maksimum olası sıralama farkı
            val rankMatch = 1.0 - (rankDiff.toDouble() / maxPossibleRankDiff)

            // Toplam eşleşme puanı (puan ve sıralama eşit ağırlıklı)
            totalScore += (scoreMatch + rankMatch) / 2
        }

        return if (departments.isNotEmpty()) totalScore / departments.size else 0.0
    }
}