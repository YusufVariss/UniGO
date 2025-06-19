package com.example.unigo.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.R
import com.example.unigo.adapters.DepartmentAdapter
import com.example.unigo.models.DepartmentType
import com.example.unigo.repository.DepartmentRepository
import com.google.android.material.appbar.MaterialToolbar

class DepartmentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var departmentTypeRadioGroup: RadioGroup
    private lateinit var openEducationCheckBox: CheckBox
    private lateinit var toolbar: MaterialToolbar
    private val departmentRepository = DepartmentRepository()
    private lateinit var departmentAdapter: DepartmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_departments)

        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupFilterListeners()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Bölümler"
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.departmentsRecyclerView)
        departmentTypeRadioGroup = findViewById(R.id.departmentTypeRadioGroup)
        openEducationCheckBox = findViewById(R.id.openEducationCheckBox)
    }

    private fun setupRecyclerView() {
        departmentAdapter = DepartmentAdapter { department ->
            // Bölüm detaylarını göster
            // TODO: Bölüm detay sayfasına yönlendir
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DepartmentsActivity)
            adapter = departmentAdapter
        }
        departmentAdapter.submitList(departmentRepository.getAllDepartments())
    }

    private fun setupFilterListeners() {
        departmentTypeRadioGroup.setOnCheckedChangeListener { _, _ -> applyFilters() }
        openEducationCheckBox.setOnCheckedChangeListener { _, _ -> applyFilters() }
    }

    private fun applyFilters() {
        val departmentType = when (departmentTypeRadioGroup.checkedRadioButtonId) {
            R.id.twoYearRadio -> DepartmentType.TWO_YEAR
            R.id.fourYearRadio -> DepartmentType.FOUR_YEAR
            else -> null
        }
        val openEducation = openEducationCheckBox.isChecked
        val allDepartments = departmentRepository.getAllDepartments()
        val filteredDepartments = when {
            departmentType != null && openEducation -> allDepartments.filter { it.type == departmentType && it.isOpenEducation }
            departmentType != null && !openEducation -> allDepartments.filter { it.type == departmentType && !it.isOpenEducation }
            departmentType == null && openEducation -> allDepartments.filter { it.isOpenEducation }
            else -> allDepartments
        }
        departmentAdapter.submitList(filteredDepartments)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}