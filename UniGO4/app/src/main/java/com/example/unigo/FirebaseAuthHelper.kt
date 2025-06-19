package com.example.unigo.utils

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import android.app.Activity
import android.content.Context
import com.google.firebase.auth.UserProfileChangeRequest
import com.example.unigo.R

object FirebaseAuthHelper {
    val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun signInWithGoogle(context: Context, data: Intent, callback: (Boolean, String?) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Google Sign-In hatası: ${e.message}")
            callback(false, e.message)
        }
    }

    fun register(email: String, password: String, username: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, profileTask.exception?.message)
                            }
                        }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        Log.d("FirebaseAuthHelper", "Şifre sıfırlama isteği: $email")
        if (email.isNotEmpty()) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseAuthHelper", "Şifre sıfırlama e-postası gönderildi")
                        onComplete(true, null)
                    } else {
                        Log.e("FirebaseAuthHelper", "Şifre sıfırlama hatası: ${task.exception?.message}")
                        onComplete(false, task.exception?.message)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseAuthHelper", "Şifre sıfırlama hatası: ${exception.message}")
                    onComplete(false, exception.message)
                }
        } else {
            Log.e("FirebaseAuthHelper", "E-posta adresi boş")
            onComplete(false, "E-posta adresi boş olamaz")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}