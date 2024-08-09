package com.ask.user

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthSource @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    suspend fun signInAnonymously(): String = suspendCoroutine { cont ->
        firebaseAuth.signInAnonymously()
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnSuccessListener {
                cont.resume(
                    it.user?.uid ?: throw Exception("Firebase Unable to create user")
                )
            }
    }

    suspend fun assignEmailToAccount(email: String) = suspendCoroutine { cont ->
        val userId = getCurrentUserId()
        val settings = ActionCodeSettings.newBuilder()
            .setUrl("https://ask-app-36527.web.app/user/$userId")
            .setAndroidPackageName("com.ask.app", true, "1.0")
            .setHandleCodeInApp(true)
            .build()
        firebaseAuth.sendSignInLinkToEmail(email, settings)
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnSuccessListener { cont.resume(Unit) }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}