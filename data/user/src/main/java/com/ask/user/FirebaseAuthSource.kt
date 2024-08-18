package com.ask.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    suspend fun signOut() = suspendCoroutine { cont ->
        firebaseAuth.signOut()
        cont.resume(Unit)
    }


    suspend fun connectWithGoogle(idToken: String) = suspendCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        (firebaseAuth.currentUser
            ?: throw Exception("User Not Logged In or Current User Not Found")).linkWithCredential(
            credential
        )
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnSuccessListener {
                cont.resume(
                    it.user ?: throw Exception("Firebase Unable to create user")
                )
            }
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser? = suspendCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnSuccessListener {
                cont.resume(
                    it.user ?: throw Exception("Firebase Unable to create user")
                )
            }
    }
}