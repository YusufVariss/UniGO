package com.example.unigo

import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmNewPasswordEditText: TextInputEditText
    private lateinit var changePasswordButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        // View'ları bul
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        // Otomatik doldurmayı tamamen devre dışı bırak
        currentPasswordEditText.apply {
            isSaveEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    newPasswordEditText.requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        newPasswordEditText.apply {
            isSaveEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    confirmNewPasswordEditText.requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        confirmNewPasswordEditText.apply {
            isSaveEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    changePassword()
                    true
                } else {
                    false
                }
            }
        }

        // Autofill baloncuğunu tamamen engelle
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentPasswordEditText.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            newPasswordEditText.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            confirmNewPasswordEditText.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        // Geri butonu
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener {
                clearFields()
                onBackPressed()
            }

        changePasswordButton.setOnClickListener {
            changePassword()
        }
    }

    private fun clearFields() {
        currentPasswordEditText.text?.clear()
        newPasswordEditText.text?.clear()
        confirmNewPasswordEditText.text?.clear()
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmNewPassword = confirmNewPasswordEditText.text.toString().trim()

        // Boş alan kontrolü
        when {
            TextUtils.isEmpty(currentPassword) -> {
                currentPasswordEditText.error = "Mevcut şifre alanı boş bırakılamaz"
                currentPasswordEditText.requestFocus()
                return
            }
            TextUtils.isEmpty(newPassword) -> {
                newPasswordEditText.error = "Yeni şifre alanı boş bırakılamaz"
                newPasswordEditText.requestFocus()
                return
            }
            TextUtils.isEmpty(confirmNewPassword) -> {
                confirmNewPasswordEditText.error = "Şifre tekrar alanı boş bırakılamaz"
                confirmNewPasswordEditText.requestFocus()
                return
            }
            newPassword != confirmNewPassword -> {
                confirmNewPasswordEditText.error = "Şifreler eşleşmiyor"
                confirmNewPasswordEditText.requestFocus()
                return
            }
            newPassword.length < 6 -> {
                newPasswordEditText.error = "Şifre en az 6 karakter olmalıdır"
                newPasswordEditText.requestFocus()
                return
            }
            newPassword == currentPassword -> {
                newPasswordEditText.error = "Yeni şifre mevcut şifre ile aynı olamaz"
                newPasswordEditText.requestFocus()
                return
            }
        }

        val user = auth.currentUser
        if (user != null) {
            // Önce mevcut şifreyi doğrula
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Şifreyi güncelle
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Şifre başarıyla değiştirildi",
                                        Toast.LENGTH_LONG).show()
                                    clearFields()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Şifre değiştirilemedi: ${updateTask.exception?.message}",
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Mevcut şifre yanlış", Toast.LENGTH_LONG).show()
                        clearFields()
                    }
                }
        }
    }
}