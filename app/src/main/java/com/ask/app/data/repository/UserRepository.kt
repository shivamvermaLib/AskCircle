package com.ask.app.data.repository

import com.ask.app.TABLE_USERS
import com.ask.app.data.models.Gender
import com.ask.app.data.models.User
import com.ask.app.data.models.UserWithLocation
import com.ask.app.data.source.local.UserDao
import com.ask.app.data.source.remote.FirebaseAuthSource
import com.ask.app.data.source.remote.FirebaseDataSource
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
//                    userWidgets = emptyList()
                )
            ).also {
                userDao.insertAll(listOf(it.user), listOf(it.userLocation))
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
                                "${userDetails.user.id}.${profilePicExtension}",
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

    suspend fun getCurrentUser(refresh: Boolean = false): UserWithLocation =
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
    }

    /* suspend fun publishWidgetsToUsers(createdPollWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) =
         withContext(dispatcher) {
             val pollId = createdPollWithOptionsAndVotesForTargetAudience.widget.id
             val list = userDataSource.findWithQuery(
                 getQuery = { ref ->
                     if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceGender.gender != Widget.GenderFilter.ALL) {
                         ref.orderByChild("user/gender")
                             .equalTo(createdPollWithOptionsAndVotesForTargetAudience.targetAudienceGender.gender.name)
                     } else if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.min != 0) {
                         ref.orderByChild("user/age")
                             .startAt(createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.min.toDouble())
                     } else if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.max != 0) {
                         ref.orderByChild("user/age")
                             .endAt(createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.max.toDouble())
                     } else {
                         ref.orderByChild("user/createdAt")
                             .endAt(System.currentTimeMillis().toDouble())
                     }
                 },
                 getPaginatedQuery = { ref ->
                     if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceGender.gender != Widget.GenderFilter.ALL) {
                         ref.orderByChild("user/gender")
                     } else if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.min != 0) {
                         ref.orderByChild("user/age")
                     } else if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.max != 0) {
                         ref.orderByChild("user/age")
                     } else {
                         ref.orderByChild("user/createdAt")
                     }
                 }
             )
             val filteredList = list.filter {
                 if (it.user.age != null &&
                     (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.min != 0 && createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.max != 0)
                 ) {
                     it.user.age in createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.min..createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.max
                 } else {
                     true
                 }
             }.filter { userWithLocation ->
                 if (createdPollWithOptionsAndVotesForTargetAudience.targetAudienceLocations.isNotEmpty()) {
                     userWithLocation.userLocation.let { userLocation ->
                         var locationList =
                             createdPollWithOptionsAndVotesForTargetAudience.targetAudienceLocations
                         userLocation.country?.let { country ->
                             locationList = locationList.filter { it.country == country }
                         }
                         userLocation.state?.let { state ->
                             locationList = locationList.filter { it.state == state }
                         }
                         userLocation.city?.let { city ->
                             locationList = locationList.filter { it.city == city }
                         }
                         locationList.isNotEmpty()
                     }
                 } else {
                     true
                 }
             }.map {
                 println("User Found: ${it.user.id}")
                 //TODO: send notification
                 val userWidget = User.UserWidget(
                     userId = it.user.id,
                     widgetId = pollId
                 )
                 async {
                     userDataSource.updateItem(
                         it.copy(
                             userWidgets = it.userWidgets + userWidget
                         )
                     )
                 }
             }
             filteredList.awaitAll()
         }*/

}