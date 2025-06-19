package com.example.unigo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.unigo.activities.UniversitiesActivity
import com.example.unigo.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupClickListeners()
        setupYksCountdown()
    }

    private fun setupClickListeners() {
        // Üniversiteler kartına tıklama
        binding.universitiesCard.setOnClickListener {
            startActivity(Intent(this, UniversitiesActivity::class.java))
        }

        // Bölümler kartına tıklama
        binding.departmentsCard.setOnClickListener {
            startActivity(Intent(this, com.example.unigo.activities.DepartmentsActivity::class.java))
        }

        // Puan hesaplama kartına tıklama
        binding.scoreInputCard.setOnClickListener {
            startActivity(Intent(this, com.example.unigo.activities.ScoreInputActivity::class.java))
        }

        // Profil kartına tıklama
        binding.profileCard.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupYksCountdown() {
        val yksCountdownText = binding.yksCountdownText
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val yksDate = Calendar.getInstance().apply {
                    set(2025, Calendar.JUNE, 21, 10, 15, 0)
                }
                val currentTime = System.currentTimeMillis()
                val diff = yksDate.timeInMillis - currentTime

                if (diff > 0) {
                    val days = diff / (1000 * 60 * 60 * 24)
                    val hours = (diff / (1000 * 60 * 60)) % 24
                    val minutes = (diff / (1000 * 60)) % 60
                    val seconds = (diff / 1000) % 60

                    val countdown = "$days gün $hours saat $minutes dakika $seconds saniye kaldı"
                    yksCountdownText.text = countdown

                    handler.postDelayed(this, 1000)
                } else {
                    yksCountdownText.text = "YKS başladı!"
                }
            }
        }
        handler.post(runnable)
    }
}