package com.ask.user

import com.ask.core.DOT
import com.ask.core.FirebaseDataSource
import com.ask.core.FirebaseOneDataSource
import com.ask.core.FirebaseStorageSource
import com.ask.core.IMAGE_SPLIT_FACTOR
import com.ask.core.ImageSizeType
import com.ask.core.UNDERSCORE
import com.ask.core.UpdatedTime
import com.ask.core.getAllImages
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class UserRepository @Inject constructor(
    private val firebaseAuthSource: FirebaseAuthSource,
    private val userDataSource: FirebaseDataSource<UserWithLocationCategory>,
    private val widgetUpdateTimeOneDataSource: FirebaseOneDataSource<UpdatedTime>,
    private val userDao: UserDao,
    @Named(TABLE_USERS) private val userStorageSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    suspend fun createUser(): UserWithLocationCategory = withContext(dispatcher) {
        val user = firebaseAuthSource.getCurrentUserId()?.let {
            getCurrentUserOptional(true)
        } ?: firebaseAuthSource.signInAnonymously().let { id ->
            userDataSource.addItem(
                UserWithLocationCategory(
                    user = User(id = id),
                    userLocation = User.UserLocation(userId = id),
                    userCategories = listOf()
                )
            ).also {
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories
                )
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
        profileByteArray: Map<ImageSizeType, ByteArray>? = null,
        userCategories: List<User.UserCategory>? = null,
    ): UserWithLocationCategory =
        withContext(dispatcher) {
            println("User repository called")
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
                            true -> profileByteArray.map {
                                userStorageSource.upload(
                                    "${userDetails.user.id}$UNDERSCORE${it.key.name}$DOT${profilePicExtension}",
                                    it.value
                                )
                            }.joinToString(separator = IMAGE_SPLIT_FACTOR)

                            else -> userDetails.user.profilePic
                        }
                    ),
                    userLocation = userDetails.userLocation.copy(
                        country = country?.takeIf { it.isNotBlank() }
                            ?: userDetails.userLocation.country,
                        updatedAt = System.currentTimeMillis()
                    ),
                    userCategories = (userCategories ?: userDetails.userCategories).map {
                        it.copy(
                            userId = getCurrentUserId(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                )
            ).also {
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories
                )
            }.also {
                widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                    updatedTime.copy(profileTime = System.currentTimeMillis())
                }
            }
        }

    suspend fun getCurrentUserOptional(refresh: Boolean = false): UserWithLocationCategory? =
        withContext(dispatcher) {
            firebaseAuthSource.getCurrentUserId()?.let { id ->
                if (refresh) {
                    userDataSource.getItem(id)
                        .also {
                            userDao.insertAll(
                                listOf(it.user),
                                listOf(it.userLocation),
                                it.userCategories
                            )
                        }
                } else {
                    userDao.getUserDetailsById(id) ?: userDataSource.getItem(id)
                        .also {
                            userDao.insertUser(it.user)
                        }
                }
            }
        }

    private suspend fun getCurrentUser(refresh: Boolean = false): UserWithLocationCategory =
        getCurrentUserOptional(refresh) ?: throw Exception("Current User not found")

    fun getCurrentUserId() =
        firebaseAuthSource.getCurrentUserId() ?: throw Exception("User not signed in")

    fun getCurrentUserLive(): Flow<UserWithLocationCategory> =
        firebaseAuthSource.getCurrentUserId()?.let { id ->
            userDao.getUserByIdLive(id).flowOn(dispatcher)
        } ?: throw Exception("User not signed in")

    suspend fun getUser(userId: String, refresh: Boolean = false,preloadImages: suspend (List<String>) -> Unit): User =
        withContext(dispatcher) {
            return@withContext if (refresh) {
                userDataSource.getItem(userId)
                    .also {
                        if (it.user.profilePic.isNullOrBlank().not()) {
                            preloadImages(it.user.profilePic.getAllImages())
                        }
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

    suspend fun clearAll() = withContext(dispatcher) {
        userDao.deleteAll()
    }

}