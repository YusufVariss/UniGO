package com.example.unigo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var resetPasswordButton: MaterialButton
    private lateinit var backToLoginButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        // View'ları bul
        emailEditText = findViewById(R.id.emailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "E-posta adresi gerekli"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Geçerli bir e-posta adresi girin"
                return@setOnClickListener
            }

            // Şifre sıfırlama e-postası gönder
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi",
                            Toast.LENGTH_LONG).show()
                        // Giriş sayfasına dön
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        when {
                            task.exception?.message?.contains("badly formatted") == true -> {
                                Toast.makeText(this, "Geçersiz e-posta adresi!", Toast.LENGTH_LONG).show()
                            }
                            task.exception?.message?.contains("no user record") == true -> {
                                Toast.makeText(this, "Bu e-posta adresi ile kayıtlı bir hesap bulunamadı!",
                                    Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                Toast.makeText(this, "Hata: ${task.exception?.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
        }

        backToLoginButton.setOnClickListener {
            finish()
        }
    }
} 