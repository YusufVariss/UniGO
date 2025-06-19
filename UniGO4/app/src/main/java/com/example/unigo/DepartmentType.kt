package com.example.unigo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class DepartmentType : Parcelable {
    TWO_YEAR,
    FOUR_YEAR,
    OPEN_EDUCATION
}