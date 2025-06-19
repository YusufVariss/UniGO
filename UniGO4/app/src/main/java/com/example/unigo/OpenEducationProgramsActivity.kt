package com.example.unigo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unigo.R
import com.example.unigo.adapters.ProgramAdapter
import com.example.unigo.models.Program

class OpenEducationProgramsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: ProgramAdapter
    private lateinit var programsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_education_programs)

        initializeViews()
        setupRecyclerView()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        programsRecyclerView = findViewById(R.id.programsRecyclerView)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        val programs = listOf(
            Program("Adalet", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Bankacılık ve Sigortacılık", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Büro Yönetimi ve Yönetici Asistanlığı", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Çağrı Merkezi Hizmetleri", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Dış Ticaret", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Emlak ve Emlak Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Halkla İlişkiler ve Tanıtım", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("İnsan Kaynakları Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("İşletme Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Lojistik", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Marka İletişimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Medya ve İletişim", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Menkul Kıymetler ve Sermaye Piyasası", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Muhasebe ve Vergi Uygulamaları", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Perakende Satış ve Mağaza Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Sağlık Kurumları İşletmeciliği", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Sosyal Hizmetler", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Spor Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Tarım", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Tıbbi Dokümantasyon ve Sekreterlik", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Turizm ve Otel İşletmeciliği", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Yerel Yönetimler", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("İktisat", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("İşletme", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Maliye", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Uluslararası İlişkiler", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Çalışma Ekonomisi ve Endüstri İlişkileri", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Kamu Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Konaklama İşletmeciliği", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Sağlık Yönetimi", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Sosyoloji", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Felsefe", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Tarih", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Türk Dili ve Edebiyatı", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Halkla İlişkiler ve Reklamcılık", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Yönetim Bilişim Sistemleri", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("Okul Öncesi Öğretmenliği", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95),
            Program("İlahiyat", "Anadolu Üniversitesi", "Eskişehir", 150, 0, 95)
        )
        adapter = ProgramAdapter(programs)
        programsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OpenEducationProgramsActivity)
            adapter = this@OpenEducationProgramsActivity.adapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}