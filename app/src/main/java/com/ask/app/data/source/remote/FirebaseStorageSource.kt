package com.ask.app.data.source.remote

import com.google.firebase.storage.StorageReference
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseStorageSource(private val storageReference: StorageReference) {

    suspend fun upload(fileName: String, filePath: String) = suspendCoroutine { cont ->
        val file = File(filePath)
        val ref = storageReference.child(fileName)
        val uploadTask = ref.putBytes(file.readBytes())
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
                cont.resume(downloadUri.path ?: throw Throwable("Unable to get the path of file"))
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