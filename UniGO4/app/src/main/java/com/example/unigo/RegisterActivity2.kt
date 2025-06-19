package com.example.unigo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginButton: MaterialButton
    private lateinit var loginText: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // View'ları bul
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        loginText = findViewById(R.id.loginText)

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Boş alan kontrolü
            when {
                TextUtils.isEmpty(fullName) -> {
                    fullNameEditText.error = "Ad Soyad alanı boş bırakılamaz"
                    fullNameEditText.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(email) -> {
                    emailEditText.error = "E-posta alanı boş bırakılamaz"
                    emailEditText.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(password) -> {
                    passwordEditText.error = "Şifre alanı boş bırakılamaz"
                    passwordEditText.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(confirmPassword) -> {
                    confirmPasswordEditText.error = "Şifre tekrar alanı boş bırakılamaz"
                    confirmPasswordEditText.requestFocus()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    confirmPasswordEditText.error = "Şifreler eşleşmiyor"
                    confirmPasswordEditText.requestFocus()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    passwordEditText.error = "Şifre en az 6 karakter olmalıdır"
                    passwordEditText.requestFocus()
                    return@setOnClickListener
                }
            }

            // Kayıt işlemi
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Kayıt başarılı
                        Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_LONG).show()
                        // Form alanlarını temizle
                        fullNameEditText.setText("")
                        emailEditText.setText("")
                        passwordEditText.setText("")
                        confirmPasswordEditText.setText("")
                        // Giriş sayfasına yönlendir
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        // Kayıt başarısız
                        when {
                            task.exception?.message?.contains("email address is already in use") == true -> {
                                Toast.makeText(this, "Bu e-posta adresi zaten kullanımda!", Toast.LENGTH_LONG).show()
                            }
                            task.exception?.message?.contains("badly formatted") == true -> {
                                Toast.makeText(this, "Geçersiz e-posta adresi!", Toast.LENGTH_LONG).show()
                            }
                            task.exception?.message?.contains("password") == true -> {
                                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                Toast.makeText(this, "Kayıt başarısız: ${task.exception?.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
        }

        // Giriş Yap butonuna tıklandığında
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Giriş Yap metnine tıklandığında
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}