package com.example.unigo.models

enum class UniversityType {
    DEVLET,
    VAKIF;

    override fun toString(): String {
        return when (this) {
            DEVLET -> "DEVLET"
            VAKIF -> "VAKIF"
        }
    }
} 