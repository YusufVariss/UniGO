package com.example.unigo.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unigo.adapters.UniversiteOnerileriAdapter
import com.example.unigo.databinding.ActivityUniversityResultsBinding
import com.example.unigo.models.ExamType
import com.example.unigo.models.University
import com.example.unigo.viewmodels.UniversityViewModel

class UniversityResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUniversityResultsBinding
    private lateinit var adapter: UniversiteOnerileriAdapter
    private lateinit var viewModel: UniversityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniversityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupObservers()

        // Intent'ten gelen verileri al
        val examType = intent.getStringExtra("exam_type")?.let { ExamType.valueOf(it) } ?: ExamType.TYT
        val score = intent.getDoubleExtra("score", 0.0)
        val rank = intent.getIntExtra("rank", -1).takeIf { it > 0 }

        // Üniversiteleri ara
        viewModel.searchUniversities(examType, score, rank)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[UniversityViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = UniversiteOnerileriAdapter { university ->
            showUniversityDetails(university)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.universities.observe(this) { universities ->
            adapter.submitList(universities)
            binding.emptyView.visibility = if (universities.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showUniversityDetails(university: University) {
        val message = buildString {
            append("${university.name}\n")
            append("Şehir: ${university.city}\n")
            append("Tür: ${university.type}\n\n")
            append("Bölümler:\n")
            university.departments.forEach { department ->
                append("• ${department.name}\n")
                append("  Fakülte: ${department.faculty}\n")
                append("  Dil: ${department.language}\n")
                append("  Süre: ${department.duration} Yıl\n")
                append("  Kontenjan: ${department.quota}\n")
                append("  Min Puan: ${department.minScore}\n")
                append("  Max Puan: ${department.maxScore}\n")
                append("  Min Sıralama: ${department.minRank}\n")
                append("  Max Sıralama: ${department.maxRank}\n\n")
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Üniversite Detayları")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .show()
    }
}