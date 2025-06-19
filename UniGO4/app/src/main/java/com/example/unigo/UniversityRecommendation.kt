package com.example.unigo.models

data class UniversityRecommendation(
    val universityName: String,
    val departmentName: String,
    val city: String,
    val type: String, // Devlet, Vakıf, vb.
    val quota: Int,
    val lastYearScore: Double,
    val lastYearRank: Int,
    val reason: String // AI'ın neden bu üniversiteyi önerdiğine dair açıklama
)