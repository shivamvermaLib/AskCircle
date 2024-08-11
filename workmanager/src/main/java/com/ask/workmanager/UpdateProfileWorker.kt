package com.ask.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.common.getExtension
import com.ask.common.getResizedImageByteArray
import com.ask.common.preLoadImages
import com.ask.core.EMPTY
import com.ask.user.Gender
import com.ask.user.UpdateProfileUseCase
import com.ask.user.User
import com.ask.widget.NotificationType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class UpdateProfileWorker @AssistedInject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            NotificationUtils.showNotification(
                applicationContext,
                NotificationType.UPDATE_PROFILE_DATA,
                -1f
            )
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            val name = inputData.getString(NAME)
            val email = inputData.getString(EMAIL)
            val gender = inputData.getString(GENDER)?.let { Gender.valueOf(it) }
            val age = inputData.getString(AGE)?.toInt()
            val profilePic = inputData.getString(PROFILE_PIC)
            val country = inputData.getString(COUNTRY)
            val userCategoriesString = inputData.getStringArray(USER_CATEGORIES)
            val userCategories =
                userCategoriesString?.map { Json.decodeFromString<User.UserCategory>(it) }

            updateProfileUseCase.invoke(
                name ?: EMPTY,
                email ?: EMPTY,
                gender,
                age,
                profilePic,
                country,
                userCategories,
                null,
                applicationContext::getExtension,
                applicationContext::getResizedImageByteArray,
                applicationContext::preLoadImages
            )
            NotificationUtils.cancelNotification(
                applicationContext,
                NotificationType.UPDATE_PROFILE_DATA
            )
            setProgress(workDataOf(STATUS to WorkerStatus.Success.name))
            Result.success(workDataOf(STATUS to WorkerStatus.Success.name))
        } catch (e: Exception) {
            setProgress(workDataOf(STATUS to WorkerStatus.Failed.name))
            e.printStackTrace()
            NotificationUtils.cancelNotification(
                applicationContext,
                NotificationType.UPDATE_PROFILE_DATA
            )
            Result.failure()
        }
    }

    companion object {

        const val NAME = "user_name"
        const val EMAIL = "user_email"
        const val GENDER = "user_gender"
        const val AGE = "user_age"
        const val PROFILE_PIC = "user_profile_pic"
        const val COUNTRY = "user_country"
        const val USER_CATEGORIES = "user_categories"
        private const val UPDATE_PROFILE = "update_profile"

        fun sendRequest(
            context: Context,
            name: String,
            email: String,
            gender: Gender?,
            age: Int?,
            profilePic: String?,
            country: String?,
            userCategories: List<User.UserCategory>?,
        ) {
            val workManager = WorkManager.getInstance(context)
            val workData = Data.Builder().apply {
                putString(NAME, name)
                putString(EMAIL, email)
                putString(GENDER, gender?.name)
                putString(AGE, age?.toString())
                putString(PROFILE_PIC, profilePic)
                putString(COUNTRY, country)
                userCategories?.let {
                    putStringArray(
                        USER_CATEGORIES,
                        userCategories.map { Json.encodeToString(it) }.toTypedArray()
                    )
                }
            }
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
//                .setRequiresStorageNotLow(true)
                .build()
            val workRequest =
                OneTimeWorkRequestBuilder<UpdateProfileWorker>()
                    .setConstraints(constraints)
                    .setInputData(workData.build())
                    .addTag(UPDATE_PROFILE)
                    .build()
            workManager.enqueueUniqueWork(
                UPDATE_PROFILE, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest
            ).let { operation ->
                operation.state.observeForever {
                    println("$it")
                }
            }
        }

        fun getWorkerFlow(context: Context): Flow<List<WorkInfo>> {
            val workManager = WorkManager.getInstance(context)
            return workManager.getWorkInfosByTagFlow(UPDATE_PROFILE)
        }
    }
}