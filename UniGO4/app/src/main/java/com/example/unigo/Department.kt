package com.example.unigo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Department(
    val name: String,
    val faculty: String,
    val language: String,
    val duration: Int,
    val quota: Int,
    val minScore: Double?,
    val maxScore: Double?,
    val minRank: Int?,
    val maxRank: Int?,
    val type: DepartmentType,
    val puanTuru: String = "",
    val isOpenEducation: Boolean = false
) : Parcelable {
    val typeText: String
        get() = when (type) {
            DepartmentType.TWO_YEAR -> if (isOpenEducation) "Açık Öğretim (2 Yıllık)" else "2 Yıllık"
            DepartmentType.FOUR_YEAR -> if (isOpenEducation) "Açık Öğretim (4 Yıllık)" else "4 Yıllık"
            DepartmentType.OPEN_EDUCATION -> "Açık Öğretim"
        }

    // Bölümün puan aralığını string olarak döndür
    val scoreRangeText: String
        get() = when {
            minScore == null || maxScore == null -> "Belirtilmemiş"
            minScore == 0.0 && maxScore == 0.0 -> "Belirtilmemiş"
            else -> "%.2f - %.2f".format(minScore, maxScore)
        }

    // Bölümün başarı sıralaması aralığını string olarak döndür
    val rankRangeText: String
        get() = when {
            minRank == null || maxRank == null -> "Belirtilmemiş"
            minRank == 0 && maxRank == 0 -> "Belirtilmemiş"
            else -> "$minRank - $maxRank"
        }

    // Bölümün dil bilgisini string olarak döndür
    val languageText: String
        get() = when (language.lowercase()) {
            "türkçe" -> "Türkçe"
            "ingilizce" -> "İngilizce"
            "almanca" -> "Almanca"
            "fransızca" -> "Fransızca"
            else -> language
        }

    // Bölümün süre bilgisini string olarak döndür
    val durationText: String
        get() = "$duration Yıl"

    // Bölümün kontenjan bilgisini string olarak döndür
    val quotaText: String
        get() = if (quota > 0) "$quota Kişi" else "Belirtilmemiş"

    // Bölümün puan türünü string olarak döndür
    val puanTuruText: String
        get() = when (puanTuru.uppercase()) {
            "SAY" -> "Sayısal"
            "EA" -> "Eşit Ağırlık"
            "SOZ" -> "Sözel"
            "DIL" -> "Dil"
            "TYT" -> "TYT"
            else -> puanTuru
        }
}