package com.ask.app.data.source.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface IFirebaseDataSource<T> {
    fun updateIdForItem(t: T, id: String): T
    fun getIdForItem(t: T): String
    fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): T?
    suspend fun addItem(t: T): T
    suspend fun updateItem(t: T): T
    suspend fun deleteItem(t: T)
    suspend fun getItem(id: String): T
    suspend fun getItemsByKey(key: String, previousId: String? = null): List<T>
    suspend fun queryItem(key: String, value: String, previousId: String? = null): List<T>
    suspend fun searchItems(key: String, value: String, previousId: String? = null): List<T>
    suspend fun clear()
}

abstract class FirebaseDataSource<T>(private val databaseReference: DatabaseReference) :
    IFirebaseDataSource<T> {

    private fun setValue(reference: DatabaseReference, t: T, continuation: Continuation<T>) {
        reference.setValue(t).addOnFailureListener {
            continuation.resumeWithException(it)
        }.addOnSuccessListener {
            continuation.resume(t)
        }
    }

    private fun getReferenceForItem(t: T): DatabaseReference {
        val key = getIdForItem(t)
        return databaseReference.child(key)
    }

    override suspend fun addItem(t: T): T = suspendCoroutine { cont ->
        val id = getIdForItem(t)
        val ref = if (id.isNotEmpty()) {
            databaseReference.child(id)
        } else {
            databaseReference.push()
        }
        val newT =
            updateIdForItem(t, ref.key ?: throw Exception("Firebase Unable to create new Id"))
        setValue(ref, newT, cont)
    }

    override suspend fun updateItem(t: T): T = suspendCoroutine { cont ->
        val ref = getReferenceForItem(t)
        setValue(ref, t, cont)
    }

    override suspend fun deleteItem(t: T): Unit = suspendCoroutine { cont ->
        val ref = getReferenceForItem(t)
        ref.removeValue().addOnFailureListener {
            cont.resumeWithException(it)
        }.addOnSuccessListener {
            cont.resume(Unit)
        }
    }

    override suspend fun getItem(id: String): T = suspendCoroutine { cont ->
        databaseReference.child(id).get().addOnFailureListener {
            cont.resumeWithException(it)
        }.addOnSuccessListener {
            if (it.exists()) {
                cont.resume(
                    getItemFromDataSnapshot(it) ?: throw Exception("Item not found:$id")
                )
            } else {
                cont.resumeWithException(Throwable("Item not found:$id"))
            }
        }
    }

    override suspend fun getItemsByKey(key: String, previousId: String?): List<T> =
        suspendCoroutine { cont ->
            if (previousId != null) {
                databaseReference.orderByChild(key).endAt(previousId)
            } else {
                databaseReference.orderByChild(key)
            }.limitToLast(20)
                .get().addOnFailureListener {
                    cont.resumeWithException(it)
                }.addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.hasChildren()) {
                        cont.resume(dataSnapshot.children.mapNotNull { getItemFromDataSnapshot(it) })
                    } else {
                        cont.resumeWithException(Throwable("Item not found for key:$key"))
                    }
                }
        }

    override suspend fun queryItem(key: String, value: String, previousId: String?): List<T> =
        suspendCoroutine { cont ->
            if (previousId != null) {
                databaseReference.orderByChild(key).endAt(previousId)
            } else {
                databaseReference.orderByChild(key).equalTo(value)
            }.limitToLast(20).get().addOnFailureListener {
                cont.resumeWithException(it)
            }.addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.hasChildren()) {
                    cont.resume(dataSnapshot.children.mapNotNull { getItemFromDataSnapshot(it) })
                } else {
                    cont.resumeWithException(Throwable("Item not found for key:$key with value:$value"))
                }
            }
        }

    override suspend fun searchItems(key: String, value: String, previousId: String?): List<T> =
        suspendCoroutine { cont ->
            if (previousId != null) {
                databaseReference.orderByChild(key).endAt(previousId)
            } else {
                databaseReference.orderByChild(key).startAt(value).endAt(value + "\uf8ff")
            }.limitToLast(20).get().addOnFailureListener {
                cont.resumeWithException(it)
            }.addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.hasChildren()) {
                    cont.resume(dataSnapshot.children.mapNotNull { getItemFromDataSnapshot(it) })
                } else {
                    cont.resumeWithException(Throwable("Item not found for key:$key with value:$value"))
                }
            }
        }

    override suspend fun clear() = suspendCoroutine { cont ->
        databaseReference.removeValue()
            .addOnSuccessListener {
                cont.resume(Unit)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }
}