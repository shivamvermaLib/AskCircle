package com.ask.core

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Query
import com.google.firebase.database.Transaction
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface IFirebaseDataSource<T> {
    fun updateIdForItem(t: T, id: String): T
    fun getIdForItem(t: T): String
    fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): T?
    fun getItemFromMutableData(mutableData: MutableData): T?
    suspend fun updateItemFromTransaction(id: String, updateItem: (T) -> T): T
    suspend fun addItem(t: T): T
    suspend fun updateItem(t: T): T
    suspend fun deleteItem(t: T)
    suspend fun deleteItemById(id: String)
    suspend fun getItem(id: String): T
    suspend fun getItemOrNull(id: String): T?
    suspend fun getItemsByKey(key: String, previousId: String? = null): List<T>
    suspend fun queryItem(key: String, value: String, previousId: String? = null): List<T>
    suspend fun searchItems(key: String, value: String, previousId: String? = null): List<T>
    suspend fun findWithQuery(
        getQuery: (DatabaseReference) -> Query,
        getPaginatedQuery: (DatabaseReference) -> Query
    ): List<T>

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

    override suspend fun updateItemFromTransaction(id: String, updateItem: (T) -> T): T =
        suspendCoroutine { cont ->
            databaseReference.child(id).runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val item = getItemFromMutableData(currentData) ?: return Transaction.success(
                        currentData
                    )
                    val updatedItem = updateItem(item)
                    currentData.value = updatedItem
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        cont.resumeWithException(error.toException())
                    } else {
                        cont.resume(getItemFromDataSnapshot(currentData ?: return) ?: return)
                    }
                }
            })
        }

    override suspend fun deleteItem(t: T): Unit = suspendCoroutine { cont ->
        val ref = getReferenceForItem(t)
        ref.removeValue().addOnFailureListener {
            cont.resumeWithException(it)
        }.addOnSuccessListener {
            cont.resume(Unit)
        }
    }

    override suspend fun deleteItemById(id: String) = suspendCoroutine { cont ->
        val ref = databaseReference.child(id)
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

    override suspend fun getItemOrNull(id: String): T? = suspendCoroutine { cont ->
        databaseReference.child(id).get().addOnFailureListener {
            cont.resume(null)
        }.addOnSuccessListener {
            if (it.exists()) {
                cont.resume(getItemFromDataSnapshot(it))
            } else {
                cont.resume(null)
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


    override suspend fun findWithQuery(
        getQuery: (DatabaseReference) -> Query,
        getPaginatedQuery: (DatabaseReference) -> Query
    ): List<T> {
        var previousId: String? = null
        val finalList = mutableListOf<T>()
        while (true) {
            val newQuery = if (previousId != null) {
                getPaginatedQuery(databaseReference).startAt(previousId)
            } else {
                getQuery(databaseReference)
            }.limitToLast(20)
            val list = suspendCoroutine<List<T>> { cont ->
                newQuery.get().addOnFailureListener {
                    cont.resumeWithException(it)
                }.addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.hasChildren()) {
                        cont.resume(dataSnapshot.children.mapNotNull { getItemFromDataSnapshot(it) })
                    } else {
                        cont.resume(emptyList())
                    }
                }
            }
            if (list.isEmpty()) {
                break
            } else {
                previousId = getIdForItem(list.last())
                finalList.addAll(list)
            }
        }
        return finalList
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

abstract class FirebaseOneDataSource<T>(
    private val databaseReference: DatabaseReference,
    private val defaultValue: T?
) {
    abstract fun getItemFromMutableData(mutableData: MutableData): T?
    abstract fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): T?
    suspend fun getItem(): T? = suspendCoroutine { cont ->
        databaseReference.get().addOnFailureListener {
            cont.resumeWithException(it)
        }.addOnSuccessListener {
            if (it.exists()) {
                cont.resume(
                    getItemFromDataSnapshot(it) ?: throw Exception("Item not found")
                )
            } else {
                cont.resume(null)
            }
        }
    }

    suspend fun updateItemFromTransaction(updateItem: (T) -> T): T? =
        suspendCoroutine { cont ->
            databaseReference.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val item = getItemFromMutableData(currentData) ?: return Transaction.success(
                        currentData.apply {
                            value = defaultValue
                        }
                    )
                    currentData.value = updateItem(item)
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        cont.resumeWithException(error.toException())
                    } else {
                        cont.resume(currentData?.let { getItemFromDataSnapshot(it) })
                    }
                }
            })
        }
}