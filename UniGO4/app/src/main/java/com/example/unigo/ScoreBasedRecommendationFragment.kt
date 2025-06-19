package com.example.unigo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.R
import com.example.unigo.adapters.UniversityRecommendationAdapter
import com.example.unigo.models.DepartmentType
import com.example.unigo.models.ExamType
import com.example.unigo.models.Score
import com.example.unigo.models.UniversityType
import com.example.unigo.repository.ScoreBasedRecommendationRepository
import com.example.unigo.repository.UniversityRecommendation
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ScoreBasedRecommendationFragment : Fragment() {
    lateinit var scoreRepository: ScoreBasedRecommendationRepository
    private lateinit var recommendationAdapter: UniversityRecommendationAdapter

    private lateinit var examTypeInput: TextInputLayout
    private lateinit var examTypeAutoComplete: AutoCompleteTextView
    private lateinit var tytScoreInput: TextInputEditText
    private lateinit var aytScoreInput: TextInputEditText
    private lateinit var ydtScoreInput: TextInputEditText
    private lateinit var minRankInput: TextInputEditText
    private lateinit var maxRankInput: TextInputEditText
    private lateinit var cityInput: AutoCompleteTextView
    private lateinit var universityTypeChipGroup: ChipGroup
    private lateinit var departmentTypeChipGroup: ChipGroup
    private lateinit var searchButton: MaterialButton
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_score_based_recommendation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupListeners()
        setupRecyclerView()
    }

    private fun initializeViews(view: View) {
        examTypeInput = view.findViewById(R.id.examTypeInput)
        examTypeAutoComplete = examTypeInput.editText as AutoCompleteTextView
        tytScoreInput = view.findViewById(R.id.tytScoreInput)
        aytScoreInput = view.findViewById(R.id.aytScoreInput)
        ydtScoreInput = view.findViewById(R.id.ydtScoreInput)
        minRankInput = view.findViewById(R.id.minRankInput)
        maxRankInput = view.findViewById(R.id.maxRankInput)
        cityInput = view.findViewById(R.id.cityInput)
        universityTypeChipGroup = view.findViewById(R.id.universityTypeChipGroup)
        departmentTypeChipGroup = view.findViewById(R.id.departmentTypeChipGroup)
        searchButton = view.findViewById(R.id.searchButton)
        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView)
        progressIndicator = view.findViewById(R.id.progressIndicator)

        // Şehir listesini ayarla
        val cities = listOf("İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Adana", "Konya", "Gaziantep", "Şanlıurfa", "Kocaeli")
        val cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        cityInput.setAdapter(cityAdapter)

        // Sınav tiplerini ayarla
        val examTypes = listOf("TYT", "SAY", "EA", "SOZ", "DIL")
        val examTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, examTypes)
        examTypeAutoComplete.setAdapter(examTypeAdapter)
    }

    private fun setupListeners() {
        searchButton.setOnClickListener {
            val score = createScoreFromInputs()
            if (score != null) {
                searchUniversities(score)
            } else {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        // Sınav tipine göre input alanlarını göster/gizle
        examTypeAutoComplete.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> { // TYT
                    tytScoreInput.visibility = View.VISIBLE
                    aytScoreInput.visibility = View.GONE
                    ydtScoreInput.visibility = View.GONE
                }
                1, 2, 3 -> { // SAY, EA, SOZ
                    tytScoreInput.visibility = View.VISIBLE
                    aytScoreInput.visibility = View.VISIBLE
                    ydtScoreInput.visibility = View.GONE
                }
                4 -> { // DIL
                    tytScoreInput.visibility = View.GONE
                    aytScoreInput.visibility = View.GONE
                    ydtScoreInput.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupRecyclerView() {
        recommendationAdapter = UniversityRecommendationAdapter { recommendation ->
            // TODO: Üniversite detay sayfasına git
            Toast.makeText(context, "${recommendation.university.name} detayları gösterilecek", Toast.LENGTH_SHORT).show()
        }

        recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recommendationAdapter
        }
    }

    private fun searchUniversities(score: Score) {
        progressIndicator.visibility = View.VISIBLE
        searchButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val recommendations: List<UniversityRecommendation> = scoreRepository.getRecommendedUniversities(
                    score = score,
                    preferredCities = if (cityInput.text.isNotEmpty()) listOf(cityInput.text.toString()) else emptyList(),
                    preferredTypes = getSelectedUniversityTypes(),
                    preferredDepartmentTypes = getSelectedDepartmentTypes()
                )

                if (recommendations.isEmpty()) {
                    Toast.makeText(context, "Kriterlerinize uygun üniversite bulunamadı", Toast.LENGTH_SHORT).show()
                }

                recommendationAdapter.updateRecommendations(recommendations)
            } catch (e: Exception) {
                Toast.makeText(context, "Bir hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressIndicator.visibility = View.GONE
                searchButton.isEnabled = true
            }
        }
    }

    private fun createScoreFromInputs(): Score? {
        val examType = when (examTypeAutoComplete.text.toString()) {
            "TYT" -> ExamType.TYT
            "SAY" -> ExamType.SAY
            "EA" -> ExamType.EA
            "SOZ" -> ExamType.SOZ
            "DIL" -> ExamType.DIL
            else -> return null
        }

        val tytScore = tytScoreInput.text.toString().toDoubleOrNull() ?: return null
        val aytScore = aytScoreInput.text.toString().toDoubleOrNull() ?: return null
        val ydtScore = ydtScoreInput.text.toString().toDoubleOrNull() ?: return null
        val minRank = minRankInput.text.toString().toIntOrNull() ?: return null
        val maxRank = maxRankInput.text.toString().toIntOrNull() ?: return null

        return Score(
            tytScore = tytScore,
            aytScore = aytScore,
            ydtScore = ydtScore,
            examType = examType,
            minRank = minRank,
            maxRank = maxRank
        )
    }

    private fun getSelectedUniversityTypes(): List<UniversityType> {
        val selectedTypes = mutableListOf<UniversityType>()
        for (i in 0 until universityTypeChipGroup.childCount) {
            val chip = universityTypeChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip
            if (chip?.isChecked == true) {
                when (chip.text) {
                    "Devlet" -> selectedTypes.add(UniversityType.DEVLET)
                    "Vakıf" -> selectedTypes.add(UniversityType.VAKIF)
                }
            }
        }
        return selectedTypes
    }

    private fun getSelectedDepartmentTypes(): List<DepartmentType> {
        val selectedTypes = mutableListOf<DepartmentType>()
        for (i in 0 until departmentTypeChipGroup.childCount) {
            val chip = departmentTypeChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip
            if (chip?.isChecked == true) {
                when (chip.text) {
                    "2 Yıllık" -> selectedTypes.add(DepartmentType.TWO_YEAR)
                    "4 Yıllık" -> selectedTypes.add(DepartmentType.FOUR_YEAR)
                }
            }
        }
        return selectedTypes
    }
}