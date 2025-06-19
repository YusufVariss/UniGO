package com.example.unigo.service

import com.example.unigo.models.University
import retrofit2.http.GET
import retrofit2.http.Query

interface UniversityService {
    @GET("universities")
    suspend fun getUniversities(
        @Query("scoreType") scoreType: String,
        @Query("score") score: Double
    ): List<University> = emptyList()

    @GET("universities/all")
    suspend fun getAllUniversities(): List<University> = emptyList()
}