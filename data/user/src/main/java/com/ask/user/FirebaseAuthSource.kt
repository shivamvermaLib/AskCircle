package com.ask.user

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

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    suspend fun signInWithGoogle(idToken: String) = suspendCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.currentUser!!.linkWithCredential(credential)
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnSuccessListener { cont.resume(firebaseAuth.currentUser) }
    }

    suspend fun signOut() = suspendCoroutine { cont ->
        firebaseAuth.signOut()
        cont.resume(Unit)
    }

    suspend fun signInWithGoogle2(idToken: String) = suspendCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnSuccessListener { cont.resume(firebaseAuth.currentUser) }
    }
}