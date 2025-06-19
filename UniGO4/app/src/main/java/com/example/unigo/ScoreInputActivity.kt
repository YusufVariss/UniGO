package com.example.unigo.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unigo.databinding.ActivityScoreInputBinding
import com.google.ai.client.generativeai.GenerativeModel

class ScoreInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreInputBinding
    private val GEMINI_API_KEY = "AIzaSyB7XcjX2VTo-RKSvu-z9V21Xf53Kgk_EcI" // Google AI Studio'dan alacağınız API anahtarı
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScoreInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gemini modelini başlat
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = GEMINI_API_KEY
        )

        // Input alanlarına maksimum değer sınırlaması ekle
        setupInputFilters()

        setupToolbar()
        setupClickListeners()
    }

    override fun onDestroy() {
        // Activity kapatılırken temizlik işlemleri
        try {
            binding.root.removeAllViews()
        } catch (e: Exception) {
            Log.e("ScoreInputActivity", "Temizlik hatası: ${e.message}")
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında standart animasyonla kapat
        super.onBackPressed()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            // Toolbar geri butonuna basıldığında standart animasyonla kapat
            onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.hesaplaButton.setOnClickListener {
            calculateScores()
        }
        binding.temizleButton.setOnClickListener {
            clearInputs()
        }
        binding.universiteOnerButton.setOnClickListener {
            showAIRecommendations()
        }
    }

    private fun calculateScores() {
        // TYT net hesaplama
        val tytTurkceNet = calculateNet(binding.tytTurkceDogru.text.toString().toIntOrNull() ?: 0, binding.tytTurkceYanlis.text.toString().toIntOrNull() ?: 0)
        val tytSosyalNet = calculateNet(binding.tytSosyalDogru.text.toString().toIntOrNull() ?: 0, binding.tytSosyalYanlis.text.toString().toIntOrNull() ?: 0)
        val tytMatematikNet = calculateNet(binding.tytMatematikDogru.text.toString().toIntOrNull() ?: 0, binding.tytMatematikYanlis.text.toString().toIntOrNull() ?: 0)
        val tytFenNet = calculateNet(binding.tytFenDogru.text.toString().toIntOrNull() ?: 0, binding.tytFenYanlis.text.toString().toIntOrNull() ?: 0)

        // AYT net hesaplama
        val aytMatematikNet = calculateNet(binding.aytMatematikDogru.text.toString().toIntOrNull() ?: 0, binding.aytMatematikYanlis.text.toString().toIntOrNull() ?: 0)
        val aytFizikNet = calculateNet(binding.aytFizikDogru.text.toString().toIntOrNull() ?: 0, binding.aytFizikYanlis.text.toString().toIntOrNull() ?: 0)
        val aytKimyaNet = calculateNet(binding.aytKimyaDogru.text.toString().toIntOrNull() ?: 0, binding.aytKimyaYanlis.text.toString().toIntOrNull() ?: 0)
        val aytBiyolojiNet = calculateNet(binding.aytBiyolojiDogru.text.toString().toIntOrNull() ?: 0, binding.aytBiyolojiYanlis.text.toString().toIntOrNull() ?: 0)
        val aytEdebiyatNet = calculateNet(binding.aytEdebiyatDogru.text.toString().toIntOrNull() ?: 0, binding.aytEdebiyatYanlis.text.toString().toIntOrNull() ?: 0)
        val aytTarih1Net = calculateNet(binding.aytTarih1Dogru.text.toString().toIntOrNull() ?: 0, binding.aytTarih1Yanlis.text.toString().toIntOrNull() ?: 0)
        val aytCografya1Net = calculateNet(binding.aytCografya1Dogru.text.toString().toIntOrNull() ?: 0, binding.aytCografya1Yanlis.text.toString().toIntOrNull() ?: 0)
        val aytTarih2Net = calculateNet(binding.aytTarih2Dogru.text.toString().toIntOrNull() ?: 0, binding.aytTarih2Yanlis.text.toString().toIntOrNull() ?: 0)
        val aytCografya2Net = calculateNet(binding.aytCografya2Dogru.text.toString().toIntOrNull() ?: 0, binding.aytCografya2Yanlis.text.toString().toIntOrNull() ?: 0)
        val aytFelsefeNet = calculateNet(binding.aytFelsefeDogru.text.toString().toIntOrNull() ?: 0, binding.aytFelsefeYanlis.text.toString().toIntOrNull() ?: 0)
        val aytDinNet = calculateNet(binding.aytDinDogru.text.toString().toIntOrNull() ?: 0, binding.aytDinYanlis.text.toString().toIntOrNull() ?: 0)
        val aytDilNet = calculateNet(binding.aytDilDogru.text.toString().toIntOrNull() ?: 0, binding.aytDilYanlis.text.toString().toIntOrNull() ?: 0)

        // Diploma notu ve OBP
        val diplomaNotu = binding.diplomaNotuInput.text.toString().replace(",", ".").toDoubleOrNull()?.coerceAtLeast(50.0) ?: 50.0
        val obp = diplomaNotu * 5.0
        val obpEk = obp * 0.12

        // TYT ham puan (Raunt/hesaplama.net ile birebir uyumlu)
        val tytHam = 100.0 + (tytTurkceNet * 3.3) + (tytSosyalNet * 3.4) + (tytMatematikNet * 3.3) + (tytFenNet * 3.4)
        val tytYerlestirme = tytHam + obpEk

        // SAY (Raunt/ÖSYM ile birebir aynı formül)
        val sayHam = 100.0 + (tytTurkceNet * 1.32) + (tytSosyalNet * 1.36) + (tytMatematikNet * 1.32) + (tytFenNet * 1.36) + (aytMatematikNet * 3.0) + (aytFizikNet * 2.85) + (aytKimyaNet * 3.07) + (aytBiyolojiNet * 3.07)
        val sayYerlestirme = sayHam + obpEk

        // EA (Raunt/ÖSYM ile birebir aynı formül)
        val eaHam = 100.0 + (tytTurkceNet * 1.32) + (tytSosyalNet * 1.36) + (tytMatematikNet * 1.32) + (tytFenNet * 1.36) + (aytMatematikNet * 3.0) + (aytEdebiyatNet * 3.0) + (aytTarih1Net * 2.8) + (aytCografya1Net * 3.33)
        val eaYerlestirme = eaHam + obpEk

        // SÖZ (Raunt/ÖSYM ile birebir aynı formül)
        val sozHam = 100.0 + (tytTurkceNet * 1.32) + (tytSosyalNet * 1.36) + (tytMatematikNet * 1.32) + (tytFenNet * 1.36) + (aytEdebiyatNet * 3.0) + (aytTarih1Net * 2.8) + (aytCografya1Net * 3.33) + (aytTarih2Net * 2.91) + (aytCografya2Net * 2.91) + (aytFelsefeNet * 3.0) + (aytDinNet * 3.33)
        val sozYerlestirme = sozHam + obpEk

        // DİL (Raunt/ÖSYM güncel katsayılar)
        val dilHam = (tytHam * 0.4) + (aytDilNet * 3.0) * 0.6
        val dilYerlestirme = dilHam + obpEk

        // Sonuçları gösterirken, ilgili testte hiç net girilmemişse o puan türünü ekranda gösterme
        binding.sonucTyt.text = if (tytTurkceNet > 0.0 || tytSosyalNet > 0.0 || tytMatematikNet > 0.0 || tytFenNet > 0.0)
            "TYT: %.5f (Ham: %.5f, Yerleştirme: %.5f)".format(tytYerlestirme, tytHam, tytYerlestirme)
        else "TYT: -"

        binding.sonucSay.text = if (aytMatematikNet > 0.0 || aytFizikNet > 0.0 || aytKimyaNet > 0.0 || aytBiyolojiNet > 0.0)
            "SAY: %.5f (Ham: %.5f, Yerleştirme: %.5f)".format(sayYerlestirme, sayHam, sayYerlestirme)
        else "SAY: -"

        binding.sonucEa.text = if (aytMatematikNet > 0.0 || aytEdebiyatNet > 0.0 || aytTarih1Net > 0.0 || aytCografya1Net > 0.0)
            "EA: %.5f (Ham: %.5f, Yerleştirme: %.5f)".format(eaYerlestirme, eaHam, eaYerlestirme)
        else "EA: -"

        binding.sonucSoz.text = if (aytEdebiyatNet > 0.0 || aytTarih1Net > 0.0 || aytCografya1Net > 0.0 || aytTarih2Net > 0.0 || aytCografya2Net > 0.0 || aytFelsefeNet > 0.0 || aytDinNet > 0.0)
            "SÖZ: %.5f (Ham: %.5f, Yerleştirme: %.5f)".format(sozYerlestirme, sozHam, sozYerlestirme)
        else "SÖZ: -"

        binding.sonucDil.text = if (aytDilNet > 0.0)
            "DİL: %.5f (Ham: %.5f, Yerleştirme: %.5f)".format(dilYerlestirme, dilHam, dilYerlestirme)
        else "DİL: -"
    }

    private fun calculateNet(dogru: Int, yanlis: Int): Double {
        return dogru - (yanlis * 0.25)
    }

    private fun clearInputs() {
        binding.apply {
            tytTurkceDogru.text?.clear()
            tytTurkceYanlis.text?.clear()
            tytSosyalDogru.text?.clear()
            tytSosyalYanlis.text?.clear()
            tytMatematikDogru.text?.clear()
            tytMatematikYanlis.text?.clear()
            tytFenDogru.text?.clear()
            tytFenYanlis.text?.clear()
            aytMatematikDogru.text?.clear()
            aytMatematikYanlis.text?.clear()
            aytFizikDogru.text?.clear()
            aytFizikYanlis.text?.clear()
            aytKimyaDogru.text?.clear()
            aytKimyaYanlis.text?.clear()
            aytBiyolojiDogru.text?.clear()
            aytBiyolojiYanlis.text?.clear()
            aytEdebiyatDogru.text?.clear()
            aytEdebiyatYanlis.text?.clear()
            aytTarih1Dogru.text?.clear()
            aytTarih1Yanlis.text?.clear()
            aytCografya1Dogru.text?.clear()
            aytCografya1Yanlis.text?.clear()
            aytTarih2Dogru.text?.clear()
            aytTarih2Yanlis.text?.clear()
            aytCografya2Dogru.text?.clear()
            aytCografya2Yanlis.text?.clear()
            aytFelsefeDogru.text?.clear()
            aytFelsefeYanlis.text?.clear()
            aytDinDogru.text?.clear()
            aytDinYanlis.text?.clear()
            aytDilDogru.text?.clear()
            aytDilYanlis.text?.clear()
            diplomaNotuInput.text?.clear()
            sonucTyt.text = "TYT: -"
            sonucSay.text = "SAY: -"
            sonucEa.text = "EA: -"
            sonucSoz.text = "SÖZ: -"
            sonucDil.text = "DİL: -"
        }
    }

    private fun setupInputFilters() {
        // Derslerin maksimum soru sayıları
        val dersMaxSorular = mapOf(
            "tytTurkce" to 40,
            "tytSosyal" to 20,
            "tytMatematik" to 40,
            "tytFen" to 20,
            "aytMatematik" to 40,
            "aytFizik" to 14,
            "aytKimya" to 13,
            "aytBiyoloji" to 13,
            "aytEdebiyat" to 24,
            "aytTarih1" to 10,
            "aytCografya1" to 6,
            "aytTarih2" to 11,
            "aytCografya2" to 11,
            "aytFelsefe" to 12,
            "aytDin" to 6,
            "aytDil" to 80
        )

        // Net oranı gösterecek TextView eşleştirmeleri (TYT + AYT)
        val netOranTextViews = mapOf(
            // TYT
            "tytTurkce" to binding.tytTurkceNetOran,
            "tytSosyal" to binding.tytSosyalNetOran,
            "tytMatematik" to binding.tytMatematikNetOran,
            "tytFen" to binding.tytFenNetOran,
            // AYT
            "aytMatematik" to binding.aytMatematikNetOran,
            "aytFizik" to binding.aytFizikNetOran,
            "aytKimya" to binding.aytKimyaNetOran,
            "aytBiyoloji" to binding.aytBiyolojiNetOran,
            "aytEdebiyat" to binding.aytEdebiyatNetOran,
            "aytTarih1" to binding.aytTarih1NetOran,
            "aytCografya1" to binding.aytCografya1NetOran,
            "aytTarih2" to binding.aytTarih2NetOran,
            "aytCografya2" to binding.aytCografya2NetOran,
            "aytFelsefe" to binding.aytFelsefeNetOran,
            "aytDin" to binding.aytDinNetOran,
            "aytDil" to binding.aytDilNetOran
        )

        // Her ders için doğru ve yanlış input alanlarını eşleştir
        val dersInputlar = mapOf(
            // TYT
            "tytTurkce" to Pair(binding.tytTurkceDogru, binding.tytTurkceYanlis),
            "tytSosyal" to Pair(binding.tytSosyalDogru, binding.tytSosyalYanlis),
            "tytMatematik" to Pair(binding.tytMatematikDogru, binding.tytMatematikYanlis),
            "tytFen" to Pair(binding.tytFenDogru, binding.tytFenYanlis),
            // AYT
            "aytMatematik" to Pair(binding.aytMatematikDogru, binding.aytMatematikYanlis),
            "aytFizik" to Pair(binding.aytFizikDogru, binding.aytFizikYanlis),
            "aytKimya" to Pair(binding.aytKimyaDogru, binding.aytKimyaYanlis),
            "aytBiyoloji" to Pair(binding.aytBiyolojiDogru, binding.aytBiyolojiYanlis),
            "aytEdebiyat" to Pair(binding.aytEdebiyatDogru, binding.aytEdebiyatYanlis),
            "aytTarih1" to Pair(binding.aytTarih1Dogru, binding.aytTarih1Yanlis),
            "aytCografya1" to Pair(binding.aytCografya1Dogru, binding.aytCografya1Yanlis),
            "aytTarih2" to Pair(binding.aytTarih2Dogru, binding.aytTarih2Yanlis),
            "aytCografya2" to Pair(binding.aytCografya2Dogru, binding.aytCografya2Yanlis),
            "aytFelsefe" to Pair(binding.aytFelsefeDogru, binding.aytFelsefeYanlis),
            "aytDin" to Pair(binding.aytDinDogru, binding.aytDinYanlis),
            "aytDil" to Pair(binding.aytDilDogru, binding.aytDilYanlis)
        )

        // Her ders için TextWatcher ekle
        dersInputlar.forEach { (dersAdi, inputlar) ->
            val maxSoru = dersMaxSorular[dersAdi] ?: return@forEach
            val dogruInput = inputlar.first
            val yanlisInput = inputlar.second
            val netOranTextView = netOranTextViews[dersAdi]

            val textWatcher = object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Boş değerleri 0 olarak al
                    val dogruText = dogruInput.text.toString()
                    val yanlisText = yanlisInput.text.toString()
                    val dogruSayi = dogruText.toIntOrNull() ?: 0
                    val yanlisSayi = yanlisText.toIntOrNull() ?: 0

                    // Net oranını güncelle
                    val net = dogruSayi - (yanlisSayi * 0.25)
                    netOranTextView?.text = "Net: %.2f/%d".format(net, maxSoru)

                    // Eğer her iki alan da doluysa ve toplam maksimum soru sayısını geçiyorsa
                    if (dogruText.isNotBlank() && yanlisText.isNotBlank() &&
                        dogruSayi + yanlisSayi > maxSoru) {
                        // Yanlış sayısını sınırla
                        val yeniYanlisSayi = maxSoru - dogruSayi
                        if (yeniYanlisSayi >= 0) {
                            yanlisInput.setText(yeniYanlisSayi.toString())
                            yanlisInput.setSelection(yeniYanlisSayi.toString().length)
                            Toast.makeText(this@ScoreInputActivity,
                                "Toplam doğru ve yanlış sayısı $maxSoru'u geçemez",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            // Her iki input alanına da TextWatcher ekle
            dogruInput.addTextChangedListener(textWatcher)
            yanlisInput.addTextChangedListener(textWatcher)

            // Maksimum değer sınırlaması için InputFilter ekle
            dogruInput.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                val newValue = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend)
                if (newValue.isEmpty() || newValue.toIntOrNull()?.let { it in 0..maxSoru } == true) {
                    null // Değer kabul edilebilir
                } else {
                    "" // Değer reddedildi
                }
            })

            yanlisInput.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                val newValue = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend)
                if (newValue.isEmpty() || newValue.toIntOrNull()?.let { it in 0..maxSoru } == true) {
                    null // Değer kabul edilebilir
                } else {
                    "" // Değer reddedildi
                }
            })
        }

        // Diploma notu için sadece basit kontrol
        binding.diplomaNotuInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    try {
                        val value = text.replace(',', '.').toDouble()
                        if (value !in 50.0..100.0) {
                            binding.diplomaNotuLayout.error = "Diploma notu 50-100 arasında olmalıdır"
                        } else {
                            binding.diplomaNotuLayout.error = null
                        }
                    } catch (e: NumberFormatException) {
                        binding.diplomaNotuLayout.error = null
                    }
                } else {
                    binding.diplomaNotuLayout.error = null
                }
            }
        })
    }

    private fun showAIRecommendations() {
        try {
            // Puanları parse et
            val tytPuan = parsePuan(binding.sonucTyt.text.toString(), "TYT")
            val sayPuan = parsePuan(binding.sonucSay.text.toString(), "SAY")
            val eaPuan = parsePuan(binding.sonucEa.text.toString(), "EA")
            val sozPuan = parsePuan(binding.sonucSoz.text.toString(), "SÖZ")
            val dilPuan = parsePuan(binding.sonucDil.text.toString(), "DİL")

            // En az bir puan hesaplanmış mı kontrol et
            if (tytPuan == 0.0 && sayPuan == 0.0 && eaPuan == 0.0 && sozPuan == 0.0 && dilPuan == 0.0) {
                Toast.makeText(this, "Lütfen önce puan hesaplayın", Toast.LENGTH_SHORT).show()
                return
            }

            // AIRecommendationsActivity'yi başlat
            val intent = Intent(this, AIRecommendationsActivity::class.java).apply {
                putExtra("TYT_SCORE", tytPuan)
                putExtra("SAY_SCORE", sayPuan)
                putExtra("EA_SCORE", eaPuan)
                putExtra("SOZ_SCORE", sozPuan)
                putExtra("DIL_SCORE", dilPuan)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("ScoreInputActivity", "Öneri gösterilirken hata: ${e.message}")
            Toast.makeText(this, "Öneriler gösterilirken bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parsePuan(text: String, prefix: String): Double {
        return try {
            if (text == "$prefix: -") return 0.0
            // Puanı al (TYT: 450.25 gibi)
            val puanText = text.substringAfter("$prefix: ")
                .substringBefore(" ")
                .trim()
            puanText.replace(",", ".").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            Log.e("ScoreInputActivity", "Puan parse hatası ($prefix): ${e.message}")
            0.0
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}