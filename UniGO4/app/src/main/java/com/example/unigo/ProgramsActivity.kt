package com.example.unigo.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.R
import com.example.unigo.adapters.ProgramAdapter
import com.example.unigo.models.DepartmentType
import com.example.unigo.models.Program
import com.example.unigo.repository.DepartmentRepository

class ProgramsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: ProgramAdapter
    private lateinit var programsRecyclerView: RecyclerView
    private lateinit var departmentTypeRadioGroup: RadioGroup
    private lateinit var openEducationCheckBox: CheckBox
    private val departmentRepository = DepartmentRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programs)

        initializeViews()
        setupRecyclerView()
        setupFilterListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        programsRecyclerView = findViewById(R.id.programsRecyclerView)
        departmentTypeRadioGroup = findViewById(R.id.departmentTypeRadioGroup)
        openEducationCheckBox = findViewById(R.id.openEducationCheckBox)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val programs = departmentRepository.getAllDepartments().map { department ->
            val typeLabel = when (department.type) {
                DepartmentType.TWO_YEAR -> "2 Yıllık"
                DepartmentType.FOUR_YEAR -> "4 Yıllık"
                DepartmentType.OPEN_EDUCATION -> "Açıköğretim"
            }
            val openEduLabel = if (department.isOpenEducation) ", Açıköğretim" else ""
            val displayName = "${department.name} ($typeLabel$openEduLabel)"
            Program(
                name = displayName,
                university = "",
                location = "",
                tytScore = 0,
                aytScore = 0,
                acceptanceRate = 0
            )
        }

        adapter = ProgramAdapter(programs)
        programsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProgramsActivity)
            adapter = this@ProgramsActivity.adapter
        }
    }

    private fun setupFilterListeners() {
        departmentTypeRadioGroup.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }

        openEducationCheckBox.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun applyFilters() {
        val departmentType = when (departmentTypeRadioGroup.checkedRadioButtonId) {
            R.id.twoYearRadio -> DepartmentType.TWO_YEAR
            R.id.fourYearRadio -> DepartmentType.FOUR_YEAR
            else -> null
        }

        val showOpenEducation = if (openEducationCheckBox.isChecked) true else null

        val filteredDepartments = departmentRepository.getAllDepartments().filter { department ->
            val typeMatch = departmentType == null || department.type == departmentType
            val openEducationMatch = showOpenEducation == null || department.isOpenEducation == showOpenEducation
            typeMatch && openEducationMatch
        }.map { department ->
            val typeLabel = when (department.type) {
                DepartmentType.TWO_YEAR -> "2 Yıllık"
                DepartmentType.FOUR_YEAR -> "4 Yıllık"
                DepartmentType.OPEN_EDUCATION -> "Açıköğretim"
            }
            val openEduLabel = if (department.isOpenEducation) ", Açıköğretim" else ""
            val displayName = "${department.name} ($typeLabel$openEduLabel)"
            Program(
                name = displayName,
                university = "",
                location = "",
                tytScore = 0,
                aytScore = 0,
                acceptanceRate = 0
            )
        }

        adapter.updateData(filteredDepartments)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}