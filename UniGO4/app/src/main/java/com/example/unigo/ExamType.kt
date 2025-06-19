package com.example.unigo.models

enum class ExamType {
    TYT,
    SAY,
    EA,
    SOZ,
    DIL;

    override fun toString(): String {
        return when (this) {
            TYT -> "TYT"
            SAY -> "SAY"
            EA -> "EA"
            SOZ -> "SÖZ"
            DIL -> "DİL"
        }
    }
}