package com.ask.user

import com.ask.core.DISPATCHER_IO
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher
) {

    suspend fun anonymousSignIn() = withContext(dispatcher) {
        createUser(firebaseAuthSource.signInAnonymously(), null, null)
    }

    suspend fun googleLogin(idToken: String) =
        withContext(dispatcher) {
            val user = firebaseAuthSource.signInWithGoogle(idToken)
            user?.let {
                createUser(it.uid, it.email, it.displayName)
            } ?: throw Exception("Unable to Google Login")
        }

    suspend fun checkCurrentUser(): UserWithLocationCategory? = withContext(dispatcher) {
        firebaseAuthSource.getCurrentUserId()?.let {
            getCurrentUserOptional(true)
        }
    }

    private suspend fun createUser(id: String, email: String?, name: String?): UserWithLocationCategory =
        withContext(dispatcher) {
            userDataSource.addItem(
                UserWithLocationCategory(
                    user = User(id = id, email = email, name = name ?: ANONYMOUS_USER),
                    userLocation = User.UserLocation(userId = id),
                    userCategories = listOf(),
                    userWidgetBookmarks = listOf()
                )
            ).also {
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories,
                    it.userWidgetBookmarks
                )
            }.also {
                widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                    updatedTime.copy(profileTime = System.currentTimeMillis())
                }
            }
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
        widgetBookmarks: List<User.UserWidgetBookmarks>? = null
    ): UserWithLocationCategory = withContext(dispatcher) {
        val profilePic =
            if (profileByteArray != null && profilePicExtension != null) profileByteArray.map {
                userStorageSource.upload(
                    "${getCurrentUserId()}$UNDERSCORE${it.key.name}$DOT${profilePicExtension}",
                    it.value
                )
            }.joinToString(separator = IMAGE_SPLIT_FACTOR)
            else null

        userDataSource.updateItemFromTransaction(getCurrentUserId()) { userDetails ->
            userDetails.copy(user = userDetails.user.copy(updatedAt = System.currentTimeMillis(),
                name = name?.takeIf { it.isNotBlank() } ?: userDetails.user.name,
                email = email?.takeIf { it.isNotBlank() } ?: userDetails.user.email,
                age = age ?: userDetails.user.age,
                gender = gender ?: userDetails.user.gender,
                profilePic = profilePic ?: userDetails.user.profilePic),
                userLocation = country?.takeIf { it.isNotBlank() }?.let {
                    userDetails.userLocation.copy(
                        country = it, updatedAt = System.currentTimeMillis()
                    )
                } ?: userDetails.userLocation,
                userCategories = (userCategories?.map {
                    it.copy(
                        userId = getCurrentUserId(), updatedAt = System.currentTimeMillis()
                    )
                }) ?: userDetails.userCategories,
                userWidgetBookmarks = (widgetBookmarks?.map {
                    it.copy(
                        userId = getCurrentUserId(), updatedAt = System.currentTimeMillis()
                    )
                } ?: userDetails.userWidgetBookmarks))
        }.also {
            /*if (!email.isNullOrBlank()) {
                firebaseAuthSource.assignEmailToAccount(email)
            }*/
            userDao.deleteCategories()
            userDao.deleteWidgetBookmarks()
            userDao.insertAll(
                listOf(it.user), listOf(it.userLocation), it.userCategories, it.userWidgetBookmarks
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
                    userDataSource.getItem(id).also {
                        userDao.insertAll(
                            listOf(it.user),
                            listOf(it.userLocation),
                            it.userCategories,
                            it.userWidgetBookmarks
                        )
                    }
                } else {
                    userDao.getUserDetailsById(id) ?: userDataSource.getItem(id).also {
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

    suspend fun getUser(
        userId: String, refresh: Boolean = false, preloadImages: suspend (List<String>) -> Unit
    ): User = withContext(dispatcher) {
        return@withContext if (refresh) {
            userDataSource.getItem(userId).also {
                if (it.user.profilePic.isNullOrBlank().not()) {
                    preloadImages(it.user.profilePic.getAllImages())
                }
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories,
                    it.userWidgetBookmarks
                )
            }.user
        } else {
            userDao.getUserById(userId) ?: userDataSource.getItem(userId).also {
                if (it.user.profilePic.isNullOrBlank().not()) {
                    preloadImages(it.user.profilePic.getAllImages())
                }
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories,
                    it.userWidgetBookmarks
                )
            }.user
        }
    }

    suspend fun getUserDetails(
        userId: String, refresh: Boolean = false, preloadImages: suspend (List<String>) -> Unit
    ): UserWithLocationCategory = withContext(dispatcher) {
        return@withContext if (refresh) {
            userDataSource.getItem(userId).also {
                if (it.user.profilePic.isNullOrBlank().not()) {
                    preloadImages(it.user.profilePic.getAllImages())
                }
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories,
                    it.userWidgetBookmarks
                )
            }
        } else {
            userDao.getUserDetailsById(userId) ?: userDataSource.getItem(userId).also {
                if (it.user.profilePic.isNullOrBlank().not()) {
                    preloadImages(it.user.profilePic.getAllImages())
                }
                userDao.insertAll(
                    listOf(it.user),
                    listOf(it.userLocation),
                    it.userCategories,
                    it.userWidgetBookmarks
                )
            }
        }
    }

    suspend fun getUserDetailList(
        userIds: List<String>,
        refresh: Boolean = false,
        preloadImages: suspend (List<String>) -> Unit
    ): List<UserWithLocationCategory> = withContext(dispatcher) {
        if (refresh) {
            userIds.map { async { userDataSource.getItem(it) } }.awaitAll()
                .also { userWithLocationCategories ->
                    userDao.insertAll(
                        userWithLocationCategories.map { it.user },
                        userWithLocationCategories.map { it.userLocation },
                        emptyList(),
                        emptyList()
                    )
                }
        } else {
            val users = userDao.getUserDetailsByIds(userIds).toMutableList()
            if (users.size != userIds.size) {
                users += userIds.filter { id -> users.any { id == it.user.id }.not() }
                    .map { async { userDataSource.getItem(it) } }.awaitAll()
                    .also { userWithLocationCategories ->
                        userDao.insertAll(
                            userWithLocationCategories.map { it.user },
                            userWithLocationCategories.map { it.userLocation },
                            emptyList(),
                            emptyList()
                        )
                    }
            }
            users
        }.also { userWithLocationCategories ->
            userWithLocationCategories.mapNotNull {
                if (it.user.profilePic.isNullOrBlank().not()) {
                    async { preloadImages(it.user.profilePic.getAllImages()) }
                } else {
                    null
                }
            }.awaitAll()
        }
    }

    suspend fun deleteUser(id: String) = withContext(dispatcher) {
        val user = userDao.getUserById(id) ?: throw Exception("user not found")
        userDataSource.deleteItemById(id).also { userDao.deleteUser(user) }.also {
            widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                updatedTime.copy(profileTime = System.currentTimeMillis())
            }
        }
    }

    suspend fun clearAll() = withContext(dispatcher) {
        userDao.deleteAll()
    }


}