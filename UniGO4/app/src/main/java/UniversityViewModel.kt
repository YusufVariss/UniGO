package com.example.unigo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.unigo.models.ExamType
import com.example.unigo.models.University
import com.example.unigo.repository.UniversityRepository
import kotlinx.coroutines.launch

class UniversityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UniversityRepository(application)

    private val _universities = MutableLiveData<List<University>>()
    val universities: LiveData<List<University>> = _universities

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun searchUniversities(examType: ExamType, score: Double, rank: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val results = repository.getUniversitiesByScore(
                    examType = examType,
                    score = score,
                    rank = rank
                )

                _universities.value = results
            } catch (e: Exception) {
                _error.value = "Üniversite verileri yüklenirken bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}