package com.example.unigo.repository

import android.content.Context
import android.util.Log
import com.example.unigo.models.University
import com.example.unigo.models.Department
import com.example.unigo.models.DepartmentType
import com.example.unigo.models.ExamType
import com.example.unigo.models.UniversityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import com.example.unigo.database.UniversityDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.emptyList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniversityRepository @Inject constructor(
    private val context: Context
) {

    // Üniversite ve bölüm verilerini memory'de tutacak cache
    private val universityCache = ConcurrentHashMap<String, University>()
    private var isDataLoaded = false

    private val gson = Gson()
    private var universities: List<University>? = null

    init {
        // Uygulama başladığında verileri yükle
        loadInitialData()
    }

    private fun loadInitialData() {
        // Global scope kullanarak veri yükleme işlemini başlat
        GlobalScope.launch(Dispatchers.IO) {
            try {
                loadUniversitiesFromJson()
            } catch (e: Exception) {
                Log.e("UniversityRepository", "Veri yükleme hatası: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadUniversitiesFromJson() = withContext(Dispatchers.IO) {
        try {
            Log.d("UniversityRepository", "JSON dosyası okunmaya başlanıyor...")

            // Assets klasöründeki dosyaları listele
            val files = context.assets.list("")
            Log.d("UniversityRepository", "Assets klasöründeki dosyalar: ${files?.joinToString()}")

            if (files == null || !files.contains("universite_taban_puanlari.json")) {
                Log.e("UniversityRepository", "universite_taban_puanlari.json dosyası bulunamadı!")
                isDataLoaded = false
                return@withContext
            }

            // JSON dosyasını oku
            val jsonString = context.assets.open("universite_taban_puanlari.json").bufferedReader().use { it.readText() }
            Log.d("UniversityRepository", "JSON dosyası okundu. Boyut: ${jsonString.length} karakter")

            if (jsonString.isEmpty()) {
                Log.e("UniversityRepository", "JSON dosyası boş!")
                isDataLoaded = false
                return@withContext
            }

            // JSON'ı parse et
            val jsonArray = JSONArray(jsonString)
            Log.d("UniversityRepository", "JSON array oluşturuldu. Eleman sayısı: ${jsonArray.length()}")

            if (jsonArray.length() == 0) {
                Log.e("UniversityRepository", "JSON array boş!")
                isDataLoaded = false
                return@withContext
            }

            // Geçici veri yapısı
            val universityMap = mutableMapOf<String, MutableList<Department>>()
            var processedCount = 0
            var errorCount = 0
            var skippedCount = 0

            // Her bir üniversite verisini işle
            for (i in 0 until jsonArray.length()) {
                try {
                    val puanObj = jsonArray.getJSONObject(i)

                    // İlk birkaç kaydı detaylı logla
                    if (i < 3) {
                        Log.d("UniversityRepository", "Örnek kayıt $i: $puanObj")
                    }

                    val uniName = puanObj.optString("universite", "").trim()
                    if (uniName.isEmpty()) {
                        skippedCount++
                        if (skippedCount <= 5) { // İlk 5 boş kaydı logla
                            Log.w("UniversityRepository", "Boş üniversite adı (kayıt $i): $puanObj")
                        }
                        continue
                    }

                    processUniversityData(puanObj, universityMap)
                    processedCount++

                    // Her 1000 kayıtta bir log
                    if (processedCount % 1000 == 0) {
                        Log.d("UniversityRepository", "İlerleme: $processedCount kayıt işlendi, $errorCount hata, $skippedCount atlanan")
                    }
                } catch (e: Exception) {
                    errorCount++
                    if (errorCount <= 5) { // İlk 5 hatayı detaylı logla
                        Log.e("UniversityRepository", "JSON objesi işleme hatası (kayıt $i): ${e.message}")
                        Log.e("UniversityRepository", "Hatalı JSON objesi: ${jsonArray.optJSONObject(i)}")
                        e.printStackTrace()
                    }
                    continue
                }
            }

            // Üniversite map'ini cache'e dönüştür
            var cacheCount = 0
            universityMap.forEach { (uniName, departments) ->
                val firstDept = departments.firstOrNull()
                if (firstDept != null) {
                    universityCache[uniName] = University(
                        name = uniName,
                        city = firstDept.faculty.split(",").firstOrNull()?.trim() ?: "",
                        type = UniversityType.DEVLET, // Varsayılan olarak DEVLET
                        departments = departments
                    )
                    cacheCount++
                }
            }

            isDataLoaded = true
            Log.d("UniversityRepository", "JSON dosyası okuma tamamlandı:")
            Log.d("UniversityRepository", "- Toplam kayıt sayısı: ${jsonArray.length()}")
            Log.d("UniversityRepository", "- Başarıyla işlenen: $processedCount")
            Log.d("UniversityRepository", "- Hata alan: $errorCount")
            Log.d("UniversityRepository", "- Atlanan: $skippedCount")
            Log.d("UniversityRepository", "- Cache'e eklenen üniversite sayısı: $cacheCount")

            if (cacheCount == 0) {
                Log.e("UniversityRepository", "Hiç üniversite cache'e eklenemedi!")
                isDataLoaded = false
            }

        } catch (e: Exception) {
            Log.e("UniversityRepository", "JSON dosyası okuma hatası: ${e.message}")
            e.printStackTrace()
            isDataLoaded = false
        }
    }

    private fun processUniversityData(puanObj: JSONObject, universityMap: MutableMap<String, MutableList<Department>>) {
        try {
            // JSON objesinin tüm alanlarını logla
            Log.d("UniversityRepository", "İşlenen JSON objesi: $puanObj")

            val uniName = puanObj.optString("universite", "").trim()
            val bolum = puanObj.optString("bolum", "").trim()
            val fakulte = puanObj.optString("fakulte", "").trim()
            val tabanPuan = puanObj.optDouble("taban_puan", 0.0)
            val tavanPuan = puanObj.optDouble("tavan_puan", 0.0)
            val basariSirasi = puanObj.optInt("basari_sirasi", 0)
            val maxBasariSirasi = puanObj.optInt("max_basari_sirasi", 0)
            val puanTuru = puanObj.optString("puan_turu", "").trim()

            // Veri doğrulama
            if (uniName.isEmpty()) {
                Log.w("UniversityRepository", "Boş üniversite adı, JSON: $puanObj")
                return
            }

            if (bolum.isEmpty()) {
                Log.w("UniversityRepository", "Boş bölüm adı, Üniversite: $uniName")
                return
            }

            if (fakulte.isEmpty()) {
                Log.w("UniversityRepository", "Boş fakülte adı, Üniversite: $uniName, Bölüm: $bolum")
                return
            }

            // Puan ve sıralama kontrolü
            if (tabanPuan <= 0 && tavanPuan <= 0) {
                Log.w("UniversityRepository", "Geçersiz puan bilgisi, Üniversite: $uniName, Bölüm: $bolum")
                return
            }

            if (basariSirasi <= 0 && maxBasariSirasi <= 0) {
                Log.w("UniversityRepository", "Geçersiz sıralama bilgisi, Üniversite: $uniName, Bölüm: $bolum")
                return
            }

            val department = Department(
                name = bolum,
                faculty = fakulte,
                language = puanObj.optString("dil", "Türkçe").trim(),
                duration = if (bolum.contains("(2 Yıllık)")) 2 else 4,
                quota = puanObj.optInt("kontenjan", 0),
                minScore = tabanPuan.takeIf { it > 0 },
                maxScore = tavanPuan.takeIf { it > 0 },
                minRank = basariSirasi.takeIf { it > 0 },
                maxRank = maxBasariSirasi.takeIf { it > 0 },
                type = if (bolum.contains("(2 Yıllık)")) DepartmentType.TWO_YEAR else DepartmentType.FOUR_YEAR,
                puanTuru = puanTuru
            )

            // Üniversite tipini belirle
            val universityType = when {
                uniName.contains("Vakıf", ignoreCase = true) -> UniversityType.VAKIF
                else -> UniversityType.DEVLET
            }

            // Üniversiteyi map'e ekle veya güncelle
            val departments = universityMap.getOrPut(uniName) { mutableListOf() }
            departments.add(department)

            // Üniversite cache'ini güncelle
            universityCache[uniName] = University(
                name = uniName,
                city = fakulte.split(",").firstOrNull()?.trim() ?: "",
                type = universityType,
                departments = departments
            )

            Log.d("UniversityRepository", "Başarıyla eklendi: $uniName - $bolum")

        } catch (e: Exception) {
            Log.e("UniversityRepository", "Bölüm verisi işleme hatası: ${e.message}")
            Log.e("UniversityRepository", "Hatalı JSON objesi: $puanObj")
            e.printStackTrace()
        }
    }

    suspend fun getUniversitiesByScore(
        examType: ExamType,
        score: Double,
        rank: Int? = null,
        tolerance: Double = 20.0
    ): List<University> = withContext(Dispatchers.IO) {
        try {
            if (!isDataLoaded) {
                Log.e("UniversityRepository", "Veriler henüz yüklenmedi")
                return@withContext emptyList()
            }

            // Cache'den üniversiteleri al ve filtrele
            val filteredUniversities = universityCache.values
                .map { university ->
                    // Her üniversite için en uygun bölümü bul
                    val bestDepartment = university.departments
                        .filter { department ->
                            // Bölüm tipi kontrolü
                            when (examType) {
                                ExamType.TYT -> department.type == DepartmentType.TWO_YEAR
                                else -> department.type == DepartmentType.FOUR_YEAR
                            }
                        }
                        .filter { department ->
                            // Puan aralığı kontrolü
                            val minScore = department.minScore ?: 0.0
                            val maxScore = department.maxScore ?: Double.MAX_VALUE
                            score >= minScore - tolerance && score <= maxScore + tolerance
                        }
                        .filter { department ->
                            // Başarı sıralaması kontrolü
                            if (rank == null || department.minRank == null) true
                            else {
                                val minRank = department.minRank ?: 0
                                val maxRank = department.maxRank ?: Int.MAX_VALUE
                                rank >= minRank - 10000 && rank <= maxRank + 10000
                            }
                        }
                        .minByOrNull { department ->
                            // En yakın puanlı bölümü bul
                            val minScore = department.minScore ?: 0.0
                            val maxScore = department.maxScore ?: Double.MAX_VALUE
                            minOf(
                                abs(minScore - score),
                                abs(maxScore - score)
                            )
                        }

                    // Üniversite ve en uygun bölümünü içeren bir çift döndür
                    Pair(university, bestDepartment)
                }
                .filter { (_, department) -> department != null } // Sadece uygun bölümü olan üniversiteleri al
                .sortedBy { (_, department) ->
                    // En yakın puanlı bölüme göre sırala
                    val minScore = department?.minScore ?: 0.0
                    val maxScore = department?.maxScore ?: Double.MAX_VALUE
                    minOf(
                        abs(minScore - score),
                        abs(maxScore - score)
                    )
                }
                .map { (university, _) -> university } // Sadece üniversiteleri döndür

            Log.d("UniversityRepository", "${filteredUniversities.size} uygun üniversite bulundu")
            filteredUniversities

        } catch (e: Exception) {
            Log.e("UniversityRepository", "Üniversite filtreleme hatası: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUniversitiesByPreferences(
        examType: ExamType,
        score: Double,
        preferredTypes: List<UniversityType>,
        city: String? = null,
        departmentTypes: List<ExamType> = emptyList(),
        minScore: Double? = null,
        sortByQuota: Boolean = false
    ): List<University> = withContext(Dispatchers.IO) {
        try {
            val db = UniversityDatabase.getInstance(context)
            val allUniversities = db.getUniversities()
            val filteredUniversities = allUniversities
                .filter { university ->
                    preferredTypes.isEmpty() || university.type in preferredTypes
                }
                .filter { university ->
                    city == null || university.city.equals(city, ignoreCase = true)
                }
                .map { university ->
                    val filteredDepartments = university.departments.filter { department ->
                        val departmentExamType = when (department.puanTuru.uppercase()) {
                            "SAY" -> ExamType.SAY
                            "EA" -> ExamType.EA
                            "SOZ" -> ExamType.SOZ
                            "DIL" -> ExamType.DIL
                            else -> ExamType.TYT
                        }
                        if (departmentTypes.isNotEmpty() && departmentExamType !in departmentTypes) {
                            return@filter false
                        }
                        if (minScore != null && (department.minScore ?: Double.MAX_VALUE) < minScore) {
                            return@filter false
                        }
                        when (examType) {
                            ExamType.TYT -> department.minScore?.let { score <= 0.0 || it <= score } ?: (score <= 0.0)
                            else -> (departmentExamType == examType && (score <= 0.0 || department.minScore?.let { it <= score } ?: true))
                        }
                    }
                    if (filteredDepartments.isEmpty()) null
                    else university.copy(departments = filteredDepartments)
                }
                .filterNotNull()
            if (filteredUniversities.isEmpty()) {
                emptyList()
            } else if (sortByQuota) {
                filteredUniversities.sortedByDescending { university ->
                    university.departments.sumOf { it.quota ?: 0 }
                }
            } else {
                filteredUniversities.sortedBy { university ->
                    university.departments.minOfOrNull { it.minScore ?: Double.MAX_VALUE } ?: Double.MAX_VALUE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllUniversities(): List<University> = withContext(Dispatchers.IO) {
        if (universities == null) {
            try {
                val jsonString = context.assets.open("universite_taban_puanlari.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<University>>() {}.type
                universities = gson.fromJson(jsonString, type)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }
        universities ?: emptyList()
    }

    suspend fun getUniversitiesByCity(city: String): List<University> = withContext(Dispatchers.IO) {
        getAllUniversities().filter { it.city.equals(city, ignoreCase = true) }
    }

    suspend fun getUniversitiesByType(type: UniversityType): List<University> = withContext(Dispatchers.IO) {
        getAllUniversities().filter { it.type == type }
    }

    suspend fun searchUniversities(query: String): List<University> = withContext(Dispatchers.IO) {
        getAllUniversities().filter { university ->
            university.name.contains(query, ignoreCase = true) ||
                    university.city.contains(query, ignoreCase = true) ||
                    university.departments.any { it.name.contains(query, ignoreCase = true) }
        }
    }

    // Veri yükleme durumunu kontrol etmek için public metod
    fun isDataLoaded(): Boolean = isDataLoaded
}