package com.ask.workmanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.ask.widget.NotificationType
import com.ask.widget.SyncUsersAndWidgetsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit


@HiltWorker
class SyncWidgetWorker @AssistedInject constructor(
    private val syncWidgetUseCase: SyncUsersAndWidgetsUseCase,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            syncWidgetUseCase.invoke(true, applicationContext::preLoadImages, {
                println("Progress: $it")
            }, {
                showNotification(it)
            })
            setProgress(workDataOf(STATUS to WorkerStatus.Success.name))
            Result.success()
        } catch (e: Exception) {
            setProgress(workDataOf(STATUS to WorkerStatus.Failed.name))
            e.printStackTrace()
            Result.failure(Data(workDataOf(ERROR to e.message)))
        }
    }

    private fun showNotification(notificationType: NotificationType) {
        val (title, message) = when (notificationType) {
            NotificationType.NEW_WIDGETS -> createWidgetTitleMessage.random()
            NotificationType.USER_VOTED_ON_YOUR_WIDGET -> userVotedTitleMessage.random()
            NotificationType.USER_NOT_VOTED_ON_WIDGET_REMINDER -> reminderNotifications.random()
        }
        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle(applicationContext.getString(title))
            .setContentText(applicationContext.getString(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(
            notificationType.ordinal,
            builder.build()
        ) // 1001 is the notification ID
    }

    companion object {

        val createWidgetTitleMessage = listOf(
            R.string.new_widget_added to R.string.a_new_widget_has_been_created_and_added_to_your_collection_check_it_out_now,
            R.string.your_widget_collection_just_got_bigger to R.string.explore_the_latest_widget_in_your_collection_open_the_app_to_see_it,
            R.string.exciting_news_new_widget_created to R.string.a_brand_new_widget_is_available_don_t_miss_out_see_it_now_in_your_collection
        )
        val userVotedTitleMessage = listOf(
            R.string.new_interaction_on_your_widget to R.string.a_user_has_just_engaged_with_your_widget_check_the_updated_results_now,
            R.string.your_widget_just_got_an_interaction to R.string.someone_has_participated_with_your_widget_view_the_latest_results_and_insights,
            R.string.widget_update_new_interaction_received to R.string.your_widget_has_received_a_new_interaction_head_over_to_see_the_current_standings,
            R.string.interaction_alert_your_widget_was_engaged_with to R.string.a_new_interaction_has_been_registered_on_your_widget_check_out_the_updated_responses,
        )

        val reminderNotifications = listOf(
            R.string.don_t_miss_out to R.string.your_input_matters_vote_on_the_widget_now_and_let_your_voice_be_heard,
            R.string.your_vote_is_needed to R.string.take_a_moment_to_vote_on_the_widget_your_opinion_can_make_a_difference,
            R.string.we_value_your_opinion to R.string.haven_t_voted_yet_cast_your_vote_and_help_shape_the_outcome,
            R.string.reminder_vote_now to R.string.don_t_forget_to_vote_on_the_widget_your_participation_is_important,
            R.string.your_opinion_counts to R.string.make_sure_your_voice_is_heard_vote_on_the_widget_today,
        )


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