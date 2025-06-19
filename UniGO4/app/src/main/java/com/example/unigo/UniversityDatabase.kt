package com.example.unigo.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.util.Log
import com.example.unigo.models.University
import com.example.unigo.models.UniversityType
import com.example.unigo.models.Department
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import com.example.unigo.models.DepartmentType

class UniversityDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "university.db"
        private const val DATABASE_VERSION = 2
        private var instance: UniversityDatabase? = null

        @Synchronized
        fun getInstance(context: Context): UniversityDatabase {
            if (instance == null) {
                instance = UniversityDatabase(context.applicationContext)
            }
            return instance!!
        }

        // Tablo ve sütun isimleri
        const val TABLE_UNIVERSITIES = "universities"
        const val TABLE_DEPARTMENTS = "departments"
        const val COLUMN_ID = "id"
        const val COLUMN_UNIVERSITY_ID = "university_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CITY = "city"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DEPARTMENT_NAME = "department_name"
        const val COLUMN_FACULTY = "faculty"
        const val COLUMN_LANGUAGE = "language"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_QUOTA = "quota"
        const val COLUMN_MIN_SCORE = "min_score"
        const val COLUMN_MAX_SCORE = "max_score"
        const val COLUMN_MIN_RANK = "min_rank"
        const val COLUMN_MAX_RANK = "max_rank"
        const val COLUMN_DEPARTMENT_TYPE = "department_type"
        const val COLUMN_PUAN_TURU = "puan_turu"
    }

    private var database: SQLiteDatabase? = null

    override fun onCreate(db: SQLiteDatabase) {
        // Üniversiteler tablosu
        val createUniversitiesTable = """
            CREATE TABLE $TABLE_UNIVERSITIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_CITY TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL
            )
        """.trimIndent()

        // Bölümler tablosu
        val createDepartmentsTable = """
            CREATE TABLE $TABLE_DEPARTMENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_UNIVERSITY_ID INTEGER NOT NULL,
                $COLUMN_DEPARTMENT_NAME TEXT NOT NULL,
                $COLUMN_FACULTY TEXT NOT NULL,
                $COLUMN_LANGUAGE TEXT NOT NULL,
                $COLUMN_DURATION INTEGER NOT NULL,
                $COLUMN_QUOTA INTEGER NOT NULL,
                $COLUMN_MIN_SCORE REAL,
                $COLUMN_MAX_SCORE REAL,
                $COLUMN_MIN_RANK INTEGER,
                $COLUMN_MAX_RANK INTEGER,
                $COLUMN_DEPARTMENT_TYPE TEXT NOT NULL,
                $COLUMN_PUAN_TURU TEXT,
                FOREIGN KEY ($COLUMN_UNIVERSITY_ID) REFERENCES $TABLE_UNIVERSITIES($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createUniversitiesTable)
        db.execSQL(createDepartmentsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DEPARTMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_UNIVERSITIES")
        onCreate(db)
    }

    @Synchronized
    fun openDatabase(): SQLiteDatabase {
        if (database == null || !database!!.isOpen) {
            database = writableDatabase
        }
        return database!!
    }

    @Synchronized
    fun closeDatabase() {
        database?.let {
            if (it.isOpen) {
                it.close()
            }
        }
        database = null
    }

    fun importUniversitiesFromJson(context: Context): Boolean {
        var reader: BufferedReader? = null
        var success = false
        var db: SQLiteDatabase? = null

        try {
            Log.d("UniversityDatabase", "Veri aktarımı başlıyor...")

            // Veritabanı bağlantısını aç
            db = openDatabase()
            if (!db.isOpen) {
                Log.e("UniversityDatabase", "Veritabanı bağlantısı açılamadı!")
                return false
            }

            // JSON dosyasının varlığını kontrol et
            val jsonFile = context.assets.list("")?.find { it == "universite_taban_puanlari.json" }
            if (jsonFile == null) {
                Log.e("UniversityDatabase", "JSON dosyası bulunamadı!")
                return false
            }

            db.beginTransaction()

            // Önce tabloları temizle
            val deletedDepts = db.delete(TABLE_DEPARTMENTS, null, null)
            val deletedUnis = db.delete(TABLE_UNIVERSITIES, null, null)
            Log.d("UniversityDatabase", "Tablolar temizlendi: Departments=$deletedDepts, Universities=$deletedUnis")

            // Taban puanları JSON'ını oku
            try {
                reader = context.assets.open("universite_taban_puanlari.json").bufferedReader()
                val jsonString = reader.use { it.readText() }
                if (jsonString.isBlank()) {
                    Log.e("UniversityDatabase", "JSON dosyası boş!")
                    return false
                }
                val jsonArray = JSONArray(jsonString)
                Log.d("UniversityDatabase", "JSON dosyası okundu, ${jsonArray.length()} kayıt bulundu")

                if (jsonArray.length() == 0) {
                    Log.e("UniversityDatabase", "JSON dizisi boş!")
                    return false
                }

                // Üniversite ve bölüm verilerini işle
                val universityMap = mutableMapOf<String, Long>()
                var universityCount = 0
                var departmentCount = 0

                // İlk geçiş: Üniversiteleri ekle
                for (i in 0 until jsonArray.length()) {
                    try {
                        val puanObj = jsonArray.getJSONObject(i)
                        val uniName = puanObj.optString("universite", "")
                        val uniCity = puanObj.optString("sehir", "")
                        val uniType = puanObj.optString("universite_turu", "DEVLET")

                        if (uniName.isBlank()) {
                            Log.w("UniversityDatabase", "Boş üniversite adı atlandı")
                            continue
                        }

                        if (!universityMap.containsKey(uniName)) {
                            val values = ContentValues().apply {
                                put(COLUMN_NAME, uniName)
                                put(COLUMN_CITY, uniCity)
                                put(COLUMN_TYPE, uniType)
                            }
                            val id = db.insert(TABLE_UNIVERSITIES, null, values)
                            if (id != -1L) {
                                universityMap[uniName] = id
                                universityCount++
                            } else {
                                Log.e("UniversityDatabase", "Üniversite eklenemedi: $uniName")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("UniversityDatabase", "Üniversite eklenirken hata (index $i): ${e.message}")
                    }
                }
                Log.d("UniversityDatabase", "$universityCount üniversite eklendi")

                if (universityCount == 0) {
                    Log.e("UniversityDatabase", "Hiç üniversite eklenemedi!")
                    return false
                }

                // İkinci geçiş: Bölümleri ekle
                for (i in 0 until jsonArray.length()) {
                    try {
                        val puanObj = jsonArray.getJSONObject(i)
                        val uniName = puanObj.optString("universite", "")
                        val uniId = universityMap[uniName] ?: continue

                        val bolumAdi = puanObj.optString("bolum", "")
                        if (bolumAdi.isBlank()) {
                            Log.w("UniversityDatabase", "Boş bölüm adı atlandı: $uniName")
                            continue
                        }

                        val values = ContentValues().apply {
                            put(COLUMN_UNIVERSITY_ID, uniId)
                            put(COLUMN_DEPARTMENT_NAME, bolumAdi)
                            put(COLUMN_FACULTY, puanObj.optString("fakulte", ""))
                            put(COLUMN_LANGUAGE, puanObj.optString("dil", "Türkçe"))
                            put(COLUMN_DURATION, if (bolumAdi.contains("(2 Yıllık)")) 2 else 4)
                            put(COLUMN_QUOTA, puanObj.optInt("kontenjan", 0))
                            put(COLUMN_MIN_SCORE, puanObj.optDouble("taban_puan", 0.0).takeIf { it > 0 })
                            put(COLUMN_MAX_SCORE, puanObj.optDouble("tavan_puan", 0.0).takeIf { it > 0 })
                            put(COLUMN_MIN_RANK, puanObj.optInt("basari_sirasi", 0).takeIf { it > 0 })
                            put(COLUMN_MAX_RANK, puanObj.optInt("max_basari_sirasi", 0).takeIf { it > 0 })
                            put(COLUMN_DEPARTMENT_TYPE, if (bolumAdi.contains("(2 Yıllık)")) "TWO_YEAR" else "FOUR_YEAR")
                            put(COLUMN_PUAN_TURU, puanObj.optString("puan_turu", ""))
                        }

                        val deptId = db.insert(TABLE_DEPARTMENTS, null, values)
                        if (deptId != -1L) {
                            departmentCount++
                        } else {
                            Log.e("UniversityDatabase", "Bölüm eklenemedi: $bolumAdi - $uniName")
                        }
                    } catch (e: Exception) {
                        Log.e("UniversityDatabase", "Bölüm eklenirken hata (index $i): ${e.message}")
                    }
                }
                Log.d("UniversityDatabase", "$departmentCount bölüm eklendi")

                if (departmentCount == 0) {
                    Log.e("UniversityDatabase", "Hiç bölüm eklenemedi!")
                    return false
                }

                db.setTransactionSuccessful()
                success = true
                Log.d("UniversityDatabase", "Veriler başarıyla aktarıldı: $universityCount üniversite, $departmentCount bölüm")

            } catch (e: Exception) {
                Log.e("UniversityDatabase", "JSON okuma hatası: ${e.message}")
                e.printStackTrace()
                return false
            }

        } catch (e: Exception) {
            Log.e("UniversityDatabase", "Veri aktarımı sırasında hata: ${e.message}")
            e.printStackTrace()
            return false
        } finally {
            try {
                db?.endTransaction()
            } catch (e: Exception) {
                Log.e("UniversityDatabase", "Transaction kapatılırken hata: ${e.message}")
            }
            try {
                reader?.close()
            } catch (e: IOException) {
                Log.e("UniversityDatabase", "Reader kapatılırken hata: ${e.message}")
            }
        }

        return success
    }

    fun getUniversities(): List<University> {
        val universities = mutableListOf<University>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_UNIVERSITIES,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_CITY, COLUMN_TYPE),
            null,
            null,
            null,
            null,
            COLUMN_NAME
        )

        cursor.use {
            while (it.moveToNext()) {
                val uniId = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME))
                val city = it.getString(it.getColumnIndexOrThrow(COLUMN_CITY))
                val type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE))

                // Üniversitenin bölümlerini al
                val departments = getDepartmentsForUniversity(uniId)

                universities.add(
                    University(
                        name = name,
                        city = city,
                        type = when (type.uppercase()) {
                            "VAKIF" -> UniversityType.VAKIF
                            else -> UniversityType.DEVLET
                        },
                        departments = departments
                    )
                )
            }
        }

        return universities
    }

    private fun getDepartmentsForUniversity(universityId: Long): List<Department> {
        val departments = mutableListOf<Department>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_DEPARTMENTS,
            null,
            "$COLUMN_UNIVERSITY_ID = ?",
            arrayOf(universityId.toString()),
            null,
            null,
            COLUMN_DEPARTMENT_NAME
        )

        cursor.use {
            while (it.moveToNext()) {
                departments.add(
                    Department(
                        name = it.getString(it.getColumnIndexOrThrow(COLUMN_DEPARTMENT_NAME)),
                        faculty = it.getString(it.getColumnIndexOrThrow(COLUMN_FACULTY)),
                        language = it.getString(it.getColumnIndexOrThrow(COLUMN_LANGUAGE)),
                        duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION)),
                        quota = it.getInt(it.getColumnIndexOrThrow(COLUMN_QUOTA)),
                        minScore = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_MIN_SCORE))) null
                        else it.getDouble(it.getColumnIndexOrThrow(COLUMN_MIN_SCORE)),
                        maxScore = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_MAX_SCORE))) null
                        else it.getDouble(it.getColumnIndexOrThrow(COLUMN_MAX_SCORE)),
                        minRank = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_MIN_RANK))) null
                        else it.getInt(it.getColumnIndexOrThrow(COLUMN_MIN_RANK)),
                        maxRank = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_MAX_RANK))) null
                        else it.getInt(it.getColumnIndexOrThrow(COLUMN_MAX_RANK)),
                        type = when (it.getString(it.getColumnIndexOrThrow(COLUMN_DEPARTMENT_TYPE))) {
                            "TWO_YEAR" -> DepartmentType.TWO_YEAR
                            else -> DepartmentType.FOUR_YEAR
                        },
                        puanTuru = it.getString(it.getColumnIndexOrThrow(COLUMN_PUAN_TURU))
                    )
                )
            }
        }

        return departments
    }
}

data class UniversityDepartment(
    val universityName: String,
    val city: String,
    val type: String,
    val departmentName: String,
    val departmentType: String,
    val minScore: Double,
    val maxScore: Double
)

// ContentValues extension function
private fun Map<String, Any>.toContentValues(): android.content.ContentValues {
    val values = android.content.ContentValues()
    forEach { (key, value) ->
        when (value) {
            is String -> values.put(key, value)
            is Int -> values.put(key, value)
            is Long -> values.put(key, value)
            is Float -> values.put(key, value)
            is Double -> values.put(key, value)
            is Boolean -> values.put(key, if (value) 1 else 0)
            else -> values.put(key, value.toString())
        }
    }
    return values
}