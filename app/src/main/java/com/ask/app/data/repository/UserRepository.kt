package com.ask.app.data.repository

import com.ask.app.DOT
import com.ask.app.TABLE_USERS
import com.ask.app.data.models.Gender
import com.ask.app.data.models.UpdatedTime
import com.ask.app.data.models.User
import com.ask.app.data.models.UserWithLocation
import com.ask.app.data.source.local.UserDao
import com.ask.app.data.source.remote.FirebaseAuthSource
import com.ask.app.data.source.remote.FirebaseDataSource
import com.ask.app.data.source.remote.FirebaseOneDataSource
import com.ask.app.data.source.remote.FirebaseStorageSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class UserRepository @Inject constructor(
    private val firebaseAuthSource: FirebaseAuthSource,
    private val userDataSource: FirebaseDataSource<UserWithLocation>,
    private val widgetUpdateTimeOneDataSource: FirebaseOneDataSource<UpdatedTime>,
    private val userDao: UserDao,
    @Named(TABLE_USERS) private val userStorageSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    suspend fun createUser(): UserWithLocation = withContext(dispatcher) {
        val user = firebaseAuthSource.getCurrentUserId()?.let {
            getCurrentUserOptional(true)
        } ?: firebaseAuthSource.signInAnonymously().let { id ->
            userDataSource.addItem(
                UserWithLocation(
                    user = User(id = id),
                    userLocation = User.UserLocation(userId = id),
                )
            ).also {
                userDao.insertAll(listOf(it.user), listOf(it.userLocation))
            }.also {
                widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                    updatedTime.copy(profileTime = System.currentTimeMillis())
                }
            }
        }
        user
    }

    suspend fun updateUser(
        name: String? = null,
        email: String? = null,
        gender: Gender? = null,
        country: String? = null,
        age: Int? = null,
        profilePicExtension: String? = null,
        profileByteArray: ByteArray? = null
    ): UserWithLocation =
        withContext(dispatcher) {
            val userDetails = getCurrentUser()
            if (name == null && email == null && gender == null && country == null && age == null && profilePicExtension == null && profileByteArray == null) {
                return@withContext userDetails
            }
            userDataSource.updateItem(
                userDetails.copy(
                    user = userDetails.user.copy(
                        updatedAt = System.currentTimeMillis(),
                        name = name?.takeIf { it.isNotBlank() } ?: userDetails.user.name,
                        email = email?.takeIf { it.isNotBlank() } ?: userDetails.user.email,
                        age = age ?: userDetails.user.age,
                        gender = gender ?: userDetails.user.gender,
                        profilePic = when (profileByteArray != null && profilePicExtension != null) {
                            true -> userStorageSource.upload(
                                "${userDetails.user.id}$DOT${profilePicExtension}",
                                profileByteArray
                            )

                            else -> userDetails.user.profilePic
                        }
                    ),
                    userLocation = userDetails.userLocation.copy(
                        country = country?.takeIf { it.isNotBlank() }
                            ?: userDetails.userLocation.country,
                    )
                )
            ).also {
                userDao.insertAll(listOf(it.user), listOf(it.userLocation))
            }.also {
                widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                    updatedTime.copy(profileTime = System.currentTimeMillis())
                }
            }
        }

    suspend fun getCurrentUserOptional(refresh: Boolean = false): UserWithLocation? =
        withContext(dispatcher) {
            firebaseAuthSource.getCurrentUserId()?.let { id ->
                if (refresh) {
                    userDataSource.getItem(id)
                        .also {
                            userDao.insertAll(listOf(it.user), listOf(it.userLocation))
                        }
                } else {
                    userDao.getUserDetailsById(id) ?: userDataSource.getItem(id)
                        .also {
                            userDao.insertUser(it.user)
                        }
                }
            }
        }

    private suspend fun getCurrentUser(refresh: Boolean = false): UserWithLocation =
        getCurrentUserOptional(refresh) ?: throw Exception("Current User not found")

    fun getCurrentUserId() =
        firebaseAuthSource.getCurrentUserId() ?: throw Exception("User not signed in")

    fun getCurrentUserLive(): Flow<UserWithLocation> =
        firebaseAuthSource.getCurrentUserId()?.let { id ->
            userDao.getUserByIdLive(id).flowOn(dispatcher)
        } ?: throw Exception("User not signed in")

    suspend fun getUser(userId: String, refresh: Boolean = false): User? =
        withContext(dispatcher) {
            return@withContext if (refresh) {
                userDataSource.getItem(userId)
                    .also {
                        userDao.insertUser(it.user)
                    }.user
            } else {
                userDao.getUserById(userId) ?: userDataSource.getItem(userId)
                    .also {
                        userDao.insertUser(it.user)
                    }.user
            }

        }

    suspend fun deleteUser(id: String) = withContext(dispatcher) {
        val user = userDao.getUserById(id) ?: throw Exception("user not found")
        userDataSource.deleteItemById(id)
            .also { userDao.deleteUser(user) }
            .also {
                widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                    updatedTime.copy(profileTime = System.currentTimeMillis())
                }
            }
    }

}