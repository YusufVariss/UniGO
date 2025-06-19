package com.example.unigo.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unigo.databinding.ActivityUniversities2Binding
import com.example.unigo.adapters.UniversiteOnerileriAdapter
import com.example.unigo.data.UniversityData
import com.example.unigo.models.University
import com.example.unigo.models.UniversityType
import kotlin.text.uppercase

class UniversitiesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUniversities2Binding
    private lateinit var adapter: UniversiteOnerileriAdapter
    private var selectedCity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniversities2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupCitySpinner()
        loadUniversities()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Üniversiteler"
        }
    }

    private fun setupRecyclerView() {
        adapter = UniversiteOnerileriAdapter { university ->
            showUniversityDetails(university)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UniversitiesActivity)
            adapter = this@UniversitiesActivity.adapter
        }
    }

    private fun setupCitySpinner() {
        val cities = UniversityData.universities.map { it.city }.distinct().sorted()
        val allCities = listOf("Tümü") + cities
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allCities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.citySpinner.adapter = cityAdapter

        binding.citySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCity = if (position == 0) null else cities[position - 1]
                loadUniversities()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedCity = null
                loadUniversities()
            }
        }
    }

    private fun loadUniversities() {
        val dataUniversities = if (selectedCity != null) {
            UniversityData.universities.filter { it.city == selectedCity }
        } else {
            UniversityData.universities
        }

        // data.University'den models.University'e dönüştürme
        val modelUniversities = dataUniversities.map { dataUni ->
            University(
                name = dataUni.name,
                city = dataUni.city,
                type = when (dataUni.type) {
                    "DEVLET" -> UniversityType.DEVLET
                    "VAKIF" -> UniversityType.VAKIF
                    else -> UniversityType.DEVLET
                },
                departments = dataUni.departments.map { dataDept ->
                    com.example.unigo.models.Department(
                        name = dataDept.name,
                        faculty = "",
                        language = "Türkçe",
                        duration = 4,
                        quota = 0,
                        minScore = null,
                        maxScore = null,
                        minRank = null,
                        maxRank = null,
                        type = dataDept.type,
                        isOpenEducation = false
                    )
                }
            )
        }
        adapter.submitList(modelUniversities)
    }

    private fun showUniversityDetails(university: University) {
        val message = buildString {
            append("${university.name}\n")
            append("Şehir: ${university.city}\n")
            append("Tür: ${university.typeText}")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Üniversite Detayları")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}