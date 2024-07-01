package com.ask.app.data.repository

import com.ask.app.checkIfUrl
import com.ask.app.data.models.User
import com.ask.app.data.models.UserWithSearchFields
import com.ask.app.data.source.remote.FirebaseAuthSource
import com.ask.app.data.source.remote.FirebaseDataSource
import com.ask.app.data.source.remote.FirebaseStorageSource
import com.ask.app.data.source.local.UserDao
import com.ask.app.extension
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class UserRepository @Inject constructor(
    private val firebaseAuthSource: FirebaseAuthSource,
    private val userDataSource: FirebaseDataSource<User>,
    private val userSearchFieldsDataSource: FirebaseDataSource<User.UserSearchFields>,
    private val userDao: UserDao,
    @Named("users") private val userStorageSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    suspend fun createUser(): UserWithSearchFields = withContext(dispatcher) {
        val uid = firebaseAuthSource.getCurrentUser() ?: firebaseAuthSource.signInAnonymously()
        val user = userDataSource.addItem(User(uid))
        userDao.insertAll(user)
        val userSearchFields =
            userSearchFieldsDataSource.addItem(User.UserSearchFields(userId = uid))
        userDao.insertAll(userSearchFields)
        UserWithSearchFields(user, userSearchFields)
    }

    suspend fun updateUser(userWithSearchFields: UserWithSearchFields): UserWithSearchFields =
        withContext(dispatcher) {
            UserWithSearchFields(
                userDataSource.updateItem(
                    userWithSearchFields.user.let { user ->
                        user.copy(
                            updatedAt = System.currentTimeMillis(),
                            profilePic = when (user.profilePic != null && user.profilePic.checkIfUrl()
                                .not()) {
                                true -> userStorageSource.upload(
                                    "${user.id}.${user.profilePic.extension()}",
                                    user.profilePic
                                )

                                else -> user.profilePic
                            }
                        )
                    }
                ).also { userDao.insertAll(it) },
                userSearchFieldsDataSource.updateItem(userWithSearchFields.userSearchFields.let { userSearchFields ->
                    userSearchFields.copy(
                        updatedAt = System.currentTimeMillis(),
                        age = userSearchFields.age,
                        gender = userSearchFields.gender,
                        location = userSearchFields.location
                    )
                }).also { userDao.insertAll(it) }
            )
        }

    suspend fun getCurrentUser(): UserWithSearchFields? = withContext(dispatcher) {
        firebaseAuthSource.getCurrentUser()?.let {
            userDao.getUserById(it) ?: UserWithSearchFields(
                userDataSource.getItem(it),
                userSearchFieldsDataSource.queryItem("userId", it).firstOrNull()
                    ?: throw Exception("User Search Field not found")
            )
        }
    }

    suspend fun getUserDetails(userId: String): UserWithSearchFields = withContext(dispatcher) {
        return@withContext userDao.getUserById(userId)
            ?: UserWithSearchFields(
                userDataSource.getItem(userId).also { userDao.insertAll(it) },
                userSearchFieldsDataSource.queryItem("userId", userId).firstOrNull()
                    ?.also { userDao.insertAll(it) }
                    ?: throw Exception("User Search Field not found")
            )
    }

    suspend fun deleteUser(id: String) = withContext(dispatcher) {
        val userWithSearchFields = userDao.getUserById(id) ?: throw Exception("user not found")
        userDataSource.deleteItem(userWithSearchFields.user)
            .also { userDao.deleteUser(userWithSearchFields.user) }
        userSearchFieldsDataSource.deleteItem(userWithSearchFields.userSearchFields)
            .also { userDao.deleteUserSearchFields(userWithSearchFields.userSearchFields) }
    }

}