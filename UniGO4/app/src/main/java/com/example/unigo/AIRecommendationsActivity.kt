package com.example.unigo.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unigo.R
import com.example.unigo.adapters.UniversityRecommendationAdapter
import com.example.unigo.databinding.ActivityAirecommendationsBinding
import com.example.unigo.models.Department
import com.example.unigo.models.DepartmentType
import com.example.unigo.models.University
import com.example.unigo.models.UniversityType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AIRecommendationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAirecommendationsBinding
    private lateinit var generativeModel: GenerativeModel
    private lateinit var adapter: UniversityRecommendationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirecommendationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GenerativeModel'i başlat
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = getString(R.string.gemini_api_key),
            )
        } catch (e: Exception) {
            Log.e("AIRecommendations", "Error initializing GenerativeModel", e)
            showError("AI modeli başlatılamadı: ${e.message}")
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadRecommendations()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = UniversityRecommendationAdapter { recommendation ->
            // TODO: Üniversite detay sayfasına git
            Toast.makeText(this, "${recommendation.university.name} detayları gösterilecek", Toast.LENGTH_SHORT).show()
        }
        binding.recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AIRecommendationsActivity)
            adapter = this@AIRecommendationsActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabRefresh.setOnClickListener { loadRecommendations() }
        binding.retryButton.setOnClickListener { loadRecommendations() }
    }

    private fun loadRecommendations() {
        // UI'ı yükleme durumuna getir
        binding.progressIndicator.visibility = View.VISIBLE
        binding.errorContainer.visibility = View.GONE
        binding.recommendationsContainer.visibility = View.GONE

        // Puanları intent'ten al
        val tytScore = intent.getDoubleExtra("TYT_SCORE", 0.0)
        val sayScore = intent.getDoubleExtra("SAY_SCORE", 0.0)
        val eaScore = intent.getDoubleExtra("EA_SCORE", 0.0)
        val sozScore = intent.getDoubleExtra("SOZ_SCORE", 0.0)
        val dilScore = intent.getDoubleExtra("DIL_SCORE", 0.0)

        // Puan özetini göster
        binding.tytScoreText.text = String.format("%.2f", tytScore)
        binding.aytScoreText.text = when {
            sayScore > 0 -> String.format("%.2f", sayScore)
            eaScore > 0 -> String.format("%.2f", eaScore)
            sozScore > 0 -> String.format("%.2f", sozScore)
            dilScore > 0 -> String.format("%.2f", dilScore)
            else -> "-"
        }
        binding.placementScoreText.text = when {
            sayScore > 0 -> String.format("%.2f", sayScore)
            eaScore > 0 -> String.format("%.2f", eaScore)
            sozScore > 0 -> String.format("%.2f", sozScore)
            dilScore > 0 -> String.format("%.2f", dilScore)
            else -> "-"
        }

        // AI prompt'unu oluştur
        val prompt = """
            Sen bir üniversite tercih danışmanısın. Aşağıdaki puanlara sahip bir öğrenci için en uygun 15 üniversite bölümünü öner:

            TYT Puanı: $tytScore
            AYT Sayısal Puanı: $sayScore
            AYT Eşit Ağırlık Puanı: $eaScore
            AYT Sözel Puanı: $sozScore
            AYT Dil Puanı: $dilScore

            Lütfen aşağıdaki kurallara göre öneriler yap:
            1. En az 15 farklı üniversite bölümü öner
            2. Her öneri için şu bilgileri ver:
               - Üniversite adı
               - Bölüm adı
               - Şehir
               - Üniversite türü (Devlet/Vakıf)
               - Kontenjan sayısı
               - Geçen yılki yerleşen son kişinin puanı
               - Geçen yılki yerleşen son kişinin sıralaması
               - Bu bölümü neden önerdiğine dair kısa bir açıklama
            3. Önerileri JSON formatında ver, örnek format:
            {
                "recommendations": [
                    {
                        "universityName": "Örnek Üniversitesi",
                        "departmentName": "Örnek Bölüm",
                        "city": "İstanbul",
                        "type": "Devlet",
                        "quota": 50,
                        "lastYearScore": 450.25,
                        "lastYearRank": 25000,
                        "reason": "Puanınıza uygun, iş imkanları yüksek bir bölüm"
                    }
                ]
            }
            4. Sadece JSON formatında yanıt ver, başka açıklama ekleme
        """.trimIndent()

        // AI'dan önerileri al
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)
                Log.d("AIRecommendations", "AI Response: ${response.text}")

                if (response.text.isNullOrEmpty()) {
                    throw Exception("AI'dan boş yanıt alındı")
                }

                val recommendations = parseRecommendations(response.text!!)
                Log.d("AIRecommendations", "Parsed recommendations: ${recommendations.size}")

                withContext(Dispatchers.Main) {
                    if (recommendations.isNotEmpty()) {
                        adapter.updateRecommendations(recommendations)
                        binding.recommendationsContainer.visibility = View.VISIBLE
                    } else {
                        showError("Öneriler alınamadı")
                    }
                    binding.progressIndicator.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("AIRecommendations", "Error getting recommendations", e)
                Log.e("AIRecommendations", "Error message: ${e.message}")
                Log.e("AIRecommendations", "Error cause: ${e.cause}")
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    showError("Öneriler yüklenirken bir hata oluştu: ${e.message}")
                    binding.progressIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun parseRecommendations(jsonText: String): List<com.example.unigo.repository.UniversityRecommendation> {
        return try {
            Log.d("AIRecommendations", "Parsing JSON: $jsonText")

            // Kod bloğu işaretleyicilerini temizle
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanJson)
            if (!jsonObject.has("recommendations")) {
                throw Exception("JSON'da 'recommendations' anahtarı bulunamadı")
            }

            val recommendationsArray = jsonObject.getJSONArray("recommendations")
            if (recommendationsArray.length() == 0) {
                throw Exception("Öneri listesi boş")
            }

            Log.d("AIRecommendations", "Found ${recommendationsArray.length()} recommendations")

            val recommendations = mutableListOf<com.example.unigo.repository.UniversityRecommendation>()

            for (i in 0 until recommendationsArray.length()) {
                try {
                    val item = recommendationsArray.getJSONObject(i)
                    Log.d("AIRecommendations", "Processing recommendation $i: ${item.toString()}")

                    // Üniversite nesnesini oluştur
                    val university = University(
                        name = item.getString("universityName"),
                        city = item.getString("city"),
                        type = when (item.getString("type")) {
                            "Devlet" -> UniversityType.DEVLET
                            "Vakıf" -> UniversityType.VAKIF
                            else -> UniversityType.DEVLET
                        },
                        departments = emptyList() // Şimdilik boş liste, sonra güncellenecek
                    )

                    // Bölüm nesnesini oluştur
                    val department = Department(
                        name = item.getString("departmentName"),
                        faculty = "", // AI'dan gelmiyor
                        language = "Türkçe", // Varsayılan
                        duration = 4, // Varsayılan
                        quota = item.getInt("quota"),
                        minScore = item.getDouble("lastYearScore"),
                        maxScore = item.getDouble("lastYearScore"), // Aynı puanı kullanıyoruz
                        minRank = item.getInt("lastYearRank"),
                        maxRank = item.getInt("lastYearRank"), // Aynı sıralamayı kullanıyoruz
                        type = DepartmentType.FOUR_YEAR, // Varsayılan
                        puanTuru = "TYT" // Varsayılan
                    )

                    recommendations.add(
                        com.example.unigo.repository.UniversityRecommendation(
                            university = university,
                            matchingDepartments = listOf(department),
                            matchScore = 1.0 // Varsayılan eşleşme puanı
                        )
                    )
                    Log.d("AIRecommendations", "Successfully added recommendation for ${university.name}")
                } catch (e: Exception) {
                    Log.e("AIRecommendations", "Error processing recommendation $i", e)
                    // Tek bir önerinin hatalı olması tüm listeyi etkilemesin
                    continue
                }
            }

            if (recommendations.isEmpty()) {
                throw Exception("Hiç geçerli öneri bulunamadı")
            }

            recommendations
        } catch (e: Exception) {
            Log.e("AIRecommendations", "Error parsing recommendations", e)
            Log.e("AIRecommendations", "Error message: ${e.message}")
            Log.e("AIRecommendations", "Error cause: ${e.cause}")
            e.printStackTrace()
            throw e // Hatayı yukarı fırlat
        }
    }

    private fun showError(message: String) {
        binding.errorContainer.visibility = View.VISIBLE
        binding.recommendationsContainer.visibility = View.GONE
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}