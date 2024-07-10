package com.ask.app.data.source.remote

import com.google.firebase.storage.StorageReference
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseStorageSource(private val storageReference: StorageReference) {

    suspend fun download(fileName: String) = suspendCoroutine { cont ->
        val ref = storageReference.child(fileName)
        val oneMegaByte: Long = 1024 * 1024
        ref.getBytes(oneMegaByte).addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            val countries = it.toString(Charsets.UTF_8)
            cont.resume(countries)
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

    suspend fun upload(fileName: String, byteArray: ByteArray) = suspendCoroutine { cont ->
        val ref = storageReference.child(fileName)
        val uploadTask = ref.putBytes(byteArray)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                cont.resume(downloadUri?.toString() ?: throw Throwable("Unable to get the path of file"))
            } else {
                cont.resumeWithException(
                    task.exception ?: Throwable("Something went wrong while uploading")
                )
            }
        }
    }

    suspend fun delete(fileName: String) = suspendCoroutine { cont ->
        val ref = storageReference.child(fileName)
        ref.delete()
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
}