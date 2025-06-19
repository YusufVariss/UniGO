package com.example.unigo

import android.app.Application
import com.google.ai.client.generativeai.GenerativeModel

class UniGOApplication : Application() {
    lateinit var generativeModel: GenerativeModel
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide dependencies here

        // Gemini modelini başlat
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyB7XcjX2VTo-RKSvu-z9V21Xf53Kgk_EcI" // Google AI Studio'dan alınan API anahtarı
        )
    }
}