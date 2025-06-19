package com.example.unigo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Score(
    val tytScore: Double,
    val aytScore: Double,
    val ydtScore: Double,
    val examType: ExamType,
    val minRank: Int,
    val maxRank: Int
) : Parcelable {
    val typeText: String
        get() = examType.toString()

    fun getTotalScore(): Double {
        return when (examType) {
            ExamType.TYT -> tytScore
            ExamType.SAY -> tytScore * 0.4 + aytScore * 0.6
            ExamType.EA -> tytScore * 0.4 + aytScore * 0.6
            ExamType.SOZ -> tytScore * 0.4 + aytScore * 0.6
            ExamType.DIL -> ydtScore
        }
    }

    fun isScoreInRange(score: Double): Boolean {
        return score in (minRank.toDouble()..maxRank.toDouble())
    }

    fun isRankInRange(rank: Int): Boolean {
        return rank in (minRank..maxRank)
    }
}