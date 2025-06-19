package com.example.unigo

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var updateProfileButton: MaterialButton
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        // View'ları bul
        emailEditText = findViewById(R.id.emailEditText)
        updateProfileButton = findViewById(R.id.updateProfileButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)

        // E-posta alanını aktif et ve otomatik doldurmayı devre dışı bırak
        emailEditText.apply {
            isEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
            isSaveEnabled = false
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        // Autofill baloncuğunu tamamen engelle
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            emailEditText.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO
        }

        // Şifre alanlarında otomatik doldurma ve önerileri devre dışı bırak
        currentPasswordEditText.apply {
            isSaveEnabled = false
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO
            }
        }
        newPasswordEditText.apply {
            isSaveEnabled = false
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO
            }
        }

        // Profil güncelleme
        updateProfileButton.setOnClickListener {
            updateProfile()
        }

        // Şifre değiştirme
        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // Çıkış yapma
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Toolbar'ı ayarla ve geri okunu etkinleştir
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser ?: return
        val newEmail = emailEditText.text.toString().trim()

        if (newEmail.isEmpty()) {
            emailEditText.error = "E-posta alanı boş bırakılamaz"
            return
        }

        if (newEmail != user.email) {
            // E-posta değişikliği varsa
            user.updateEmail(newEmail)
                .addOnSuccessListener {
                    Toast.makeText(this, "E-posta güncellendi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "E-posta güncellenemedi: ${it.message}",
                        Toast.LENGTH_LONG).show()
                }
        }
    }
} 