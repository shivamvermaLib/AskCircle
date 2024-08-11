package com.ask.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.common.preLoadImages
import com.ask.core.AppSharedPreference
import com.ask.widget.NotificationType
import com.ask.widget.SyncUsersAndWidgetsUseCase
import com.ask.workmanager.NotificationUtils.cancelNotification
import com.ask.workmanager.NotificationUtils.showNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit


@HiltWorker
class SyncWidgetWorker @AssistedInject constructor(
    private val syncWidgetUseCase: SyncUsersAndWidgetsUseCase,
    private val appSharedPreference: AppSharedPreference,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            showNotification(applicationContext, NotificationType.SYNC_DATA, 0f)
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            val notificationSettings = appSharedPreference.getNotificationsFlow().firstOrNull()
            syncWidgetUseCase.invoke(
                isSplash = false,
                isConnected = true,
                preloadImages = applicationContext::preLoadImages,
                onProgress = {
                    println("Progress: $it")
                    showNotification(applicationContext, NotificationType.SYNC_DATA, it)
                },
                onNotification = {
                    val show = when (it) {
                        NotificationType.NEW_WIDGETS -> notificationSettings?.newWidgetNotification
                        NotificationType.USER_VOTED_ON_YOUR_WIDGET -> notificationSettings?.voteOnYourWidgetNotification
                        NotificationType.USER_NOT_VOTED_ON_WIDGET_REMINDER -> notificationSettings?.votingReminderNotification
                        NotificationType.SYNC_DATA -> true
                        NotificationType.UPDATE_PROFILE_DATA -> true
                        NotificationType.CREATE_WIDGET -> true
                        NotificationType.WIDGET_TIME_END -> notificationSettings?.widgetTimeEndNotification
                    }
                    if (show == null || show) {
                        showNotification(applicationContext, it)
                    }
                })
            setProgress(workDataOf(STATUS to WorkerStatus.Success.name))
            cancelNotification(applicationContext, NotificationType.SYNC_DATA)
            Result.success()
        } catch (e: Exception) {
            setProgress(workDataOf(STATUS to WorkerStatus.Failed.name))
            e.printStackTrace()
            cancelNotification(applicationContext, NotificationType.SYNC_DATA)
            Result.failure(Data(workDataOf(ERROR to e.message)))
        }
    }


    companion object {

        fun sendRequest(context: Context, syncTimeInMinutes: Long) {
            val workManager = WorkManager.getInstance(context)
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
//                .setRequiresStorageNotLow(true)
                .build()
            val myWork = PeriodicWorkRequestBuilder<SyncWidgetWorker>(
                syncTimeInMinutes, TimeUnit.MINUTES
            ).setConstraints(constraints)
                .addTag(SYNC_WIDGET_WORK)
                .build()
            workManager.enqueueUniquePeriodicWork(
                SYNC_WIDGET_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                myWork
            )
        }

        fun getWorkerFlow(context: Context): Flow<List<WorkInfo>> {
            val workManager = WorkManager.getInstance(context)
            return workManager.getWorkInfosByTagFlow(SYNC_WIDGET_WORK)
        }

    }

}