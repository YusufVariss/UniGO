package com.example.unigo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class University(
    val name: String,
    val city: String,
    val type: UniversityType,
    val departments: List<Department> = emptyList()
) : Parcelable {
    val typeText: String
        get() = when (type) {
            UniversityType.DEVLET -> "Devlet"
            UniversityType.VAKIF -> "Vakıf"
        }
}


