package com.example.unigo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unigo.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var forgotPasswordButton: MaterialButton
    private lateinit var googleSignInButton: MaterialButton

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.let {
                Log.d(TAG, "Google Sign In başarılı: ${it.email}")
                firebaseAuthWithGoogle(it.idToken!!)
            } ?: run {
                Log.e(TAG, "Google hesabı alınamadı")
                Toast.makeText(this, "Google hesabı alınamadı", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign In hatası: ${e.statusCode}", e)
            when (e.statusCode) {
                7 -> Toast.makeText(this, "İnternet bağlantınızı kontrol edin", Toast.LENGTH_SHORT).show()
                12500 -> Toast.makeText(this, "Google giriş yapılandırması eksik", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "Google girişi başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Google Sign In yapılandırması
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // View'ları bul
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        // E-posta alanını temizle
        emailEditText.setText("")

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Boş alan kontrolü
            when {
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
            }

            // Giriş işlemi
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Giriş başarılı
                        Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                        // Ana sayfaya yönlendir
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Hata durumunda
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                // Hesap bulunamadı
                                AlertDialog.Builder(this)
                                    .setTitle("Giriş Başarısız")
                                    .setMessage("Bu e-posta adresi ile kayıtlı bir hesap bulunamadı. Lütfen kayıt olun.")
                                    .setPositiveButton("Kayıt Ol") { _, _ ->
                                        startActivity(Intent(this, RegisterActivity::class.java))
                                    }
                                    .setNegativeButton("İptal", null)
                                    .show()
                            }
                            else -> {
                                // Diğer hatalar için genel mesaj
                                Toast.makeText(this, "Giriş başarısız: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        try {
            // Önceki oturumu kapat
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google girişi başlatılamadı", e)
            Toast.makeText(this, "Google girişi başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Firebase kimlik doğrulama başarılı")
                        Toast.makeText(this, "Google ile giriş başarılı!", Toast.LENGTH_SHORT).show()
                        try {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } catch (e: Exception) {
                            Log.e(TAG, "Ana sayfaya yönlendirme hatası", e)
                            Toast.makeText(this, "Ana sayfaya yönlendirilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "Firebase kimlik doğrulama hatası", task.exception)
                        Toast.makeText(this, "Google ile giriş başarısız: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Kimlik doğrulama hatası", e)
            Toast.makeText(this, "Kimlik doğrulama hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}