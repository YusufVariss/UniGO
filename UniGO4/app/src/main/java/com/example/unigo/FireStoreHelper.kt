package com.example.unigo.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.io.InputStream

object FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private val universitiesCollection = db.collection("universities")
    private val gson = Gson()

    // JSON verilerini Firebase'e aktar
    fun importUniversitiesFromJson(inputStream: InputStream, onComplete: (Boolean, String?) -> Unit) {
        try {
            // JSON dosyasını oku
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            var successCount = 0
            var errorCount = 0

            // Batch işlemi başlat
            val batch = db.batch()

            // Her üniversite için
            for (i in 0 until jsonArray.length()) {
                try {
                    val university = jsonArray.getJSONObject(i)
                    val universityData = mapOf(
                        "name" to university.getString("name"),
                        "city" to university.getString("city"),
                        "type" to university.getString("type"),
                        "departments" to university.getJSONArray("departments").toString()
                    )

                    // Firestore'a kaydet
                    val docRef = universitiesCollection.document(university.getString("id"))
                    batch.set(docRef, universityData)
                    successCount++
                } catch (e: Exception) {
                    errorCount++
                    println("Üniversite aktarılırken hata: ${e.message}")
                }
            }

            // Batch işlemini commit et
            batch.commit()
                .addOnSuccessListener {
                    val message = if (errorCount > 0) {
                        "$successCount üniversite başarıyla aktarıldı, $errorCount hata oluştu"
                    } else {
                        "$successCount üniversite başarıyla aktarıldı"
                    }
                    onComplete(true, message)
                }
                .addOnFailureListener { e ->
                    onComplete(false, "Veri aktarımı sırasında hata: ${e.message}")
                }

        } catch (e: Exception) {
            onComplete(false, "JSON dosyası okunurken hata: ${e.message}")
        }
    }

    // Puan türüne göre üniversiteleri getir
    fun getUniversitiesByScore(scoreType: String, score: Double, onComplete: (List<University>?, String?) -> Unit) {
        universitiesCollection
            .get()
            .addOnSuccessListener { documents ->
                val universities = mutableListOf<University>()

                for (doc in documents) {
                    try {
                        val data = doc.data
                        val departmentsJson = JSONArray(data["departments"] as String)
                        val departments = mutableListOf<Department>()

                        for (i in 0 until departmentsJson.length()) {
                            val dept = departmentsJson.getJSONObject(i)
                            val scoresJson = dept.getJSONArray("scores")
                            val scores = mutableListOf<Score>()

                            for (j in 0 until scoresJson.length()) {
                                val scoreObj = scoresJson.getJSONObject(j)
                                if (scoreObj.getString("type") == scoreType &&
                                    scoreObj.getDouble("min_score") <= score &&
                                    scoreObj.getDouble("max_score") >= score) {

                                    scores.add(Score(
                                        type = scoreObj.getString("type"),
                                        minScore = scoreObj.getDouble("min_score"),
                                        maxScore = scoreObj.getDouble("max_score")
                                    ))
                                }
                            }

                            if (scores.isNotEmpty()) {
                                departments.add(Department(
                                    name = dept.getString("name"),
                                    type = dept.getString("type"),
                                    scores = scores
                                ))
                            }
                        }

                        if (departments.isNotEmpty()) {
                            universities.add(University(
                                name = data["name"] as String,
                                city = data["city"] as String,
                                type = data["type"] as String,
                                departments = departments
                            ))
                        }
                    } catch (e: Exception) {
                        println("Üniversite verisi işlenirken hata: ${e.message}")
                    }
                }

                onComplete(universities, null)
            }
            .addOnFailureListener { e ->
                onComplete(null, e.message)
            }
    }

    // Kullanıcı profili işlemleri
    fun saveUserProfile(userId: String, userData: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        db.collection("users")
            .document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getUserProfile(userId: String, onComplete: (Map<String, Any>?, String?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(document.data, null)
                } else {
                    onComplete(null, "Kullanıcı profili bulunamadı")
                }
            }
            .addOnFailureListener { e ->
                onComplete(null, e.message)
            }
    }

    suspend fun importDepartmentsFromJson(inputStream: InputStream) {
        try {
            // JSON dosyasını oku
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // JSON'ı List<Department> nesnesine dönüştür
            val type = object : TypeToken<List<Department>>() {}.type
            val departments: List<Department> = gson.fromJson(jsonString, type)

            // Her bölümü Firestore'a kaydet
            departments.forEach { department ->
                // Otomatik ID ile yeni döküman oluştur
                db.collection("departments")
                    .add(department) // add() metodu otomatik ID oluşturur
                    .await()
            }
        } catch (e: Exception) {
            throw Exception("Bölümler aktarılırken hata oluştu: ${e.message}")
        }
    }

    suspend fun importUniversitiesFromJson(inputStream: InputStream) {
        try {
            // JSON dosyasını oku
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // JSON'ı List<University> nesnesine dönüştür
            val type = object : TypeToken<List<University>>() {}.type
            val universities: List<University> = gson.fromJson(jsonString, type)

            // Her üniversiteyi Firestore'a kaydet
            universities.forEach { university ->
                // Otomatik ID ile yeni döküman oluştur
                db.collection("universities")
                    .add(university) // add() metodu otomatik ID oluşturur
                    .await()
            }
        } catch (e: Exception) {
            throw Exception("Üniversiteler aktarılırken hata oluştu: ${e.message}")
        }
    }
}

// Veri modelleri
data class Score(
    val type: String,
    val minScore: Double,
    val maxScore: Double
)

data class Department(
    val name: String,
    val type: String,
    val scores: List<Score>
)

data class University(
    val name: String,
    val city: String,
    val type: String,
    val departments: List<Department>
)