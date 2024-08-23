package com.ask.workmanager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ask.widget.NotificationType

object NotificationUtils {
    private val createWidgetTitleMessage = listOf(
        R.string.new_widget_added to R.string.a_new_widget_has_been_created_and_added_to_your_collection_check_it_out_now,
        R.string.your_widget_collection_just_got_bigger to R.string.explore_the_latest_widget_in_your_collection_open_the_app_to_see_it,
        R.string.exciting_news_new_widget_created to R.string.a_brand_new_widget_is_available_don_t_miss_out_see_it_now_in_your_collection
    )
    private val userVotedTitleMessage = listOf(
        R.string.new_interaction_on_your_widget to R.string.a_user_has_just_engaged_with_your_widget_check_the_updated_results_now,
        R.string.your_widget_just_got_an_interaction to R.string.someone_has_participated_with_your_widget_view_the_latest_results_and_insights,
        R.string.widget_update_new_interaction_received to R.string.your_widget_has_received_a_new_interaction_head_over_to_see_the_current_standings,
        R.string.interaction_alert_your_widget_was_engaged_with to R.string.a_new_interaction_has_been_registered_on_your_widget_check_out_the_updated_responses,
    )

    private val reminderNotifications = listOf(
        R.string.don_t_miss_out to R.string.your_input_matters_vote_on_the_widget_now_and_let_your_voice_be_heard,
        R.string.your_vote_is_needed to R.string.take_a_moment_to_vote_on_the_widget_your_opinion_can_make_a_difference,
        R.string.we_value_your_opinion to R.string.haven_t_voted_yet_cast_your_vote_and_help_shape_the_outcome,
        R.string.reminder_vote_now to R.string.don_t_forget_to_vote_on_the_widget_your_participation_is_important,
        R.string.your_opinion_counts to R.string.make_sure_your_voice_is_heard_vote_on_the_widget_today,
    )


    fun showNotification(
        applicationContext: Context,
        notificationType: NotificationType,
        progress: Float? = null
    ) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val (title, message) = when (notificationType) {
            NotificationType.NEW_WIDGETS -> createWidgetTitleMessage.random()
            NotificationType.USER_VOTED_ON_YOUR_WIDGET -> userVotedTitleMessage.random()
            NotificationType.USER_NOT_VOTED_ON_WIDGET_REMINDER -> reminderNotifications.random()
            NotificationType.SYNC_DATA -> R.string.fetching_latest_data to R.string.we_re_fetching_the_latest_updates_for_you
            NotificationType.UPDATE_PROFILE_DATA -> R.string.updating_profile to R.string.your_profile_information_is_being_updated_please_wait
            NotificationType.CREATE_WIDGET -> R.string.creating_widget to R.string.your_widget_is_being_created_please_wait
            NotificationType.WIDGET_TIME_END -> R.string.your_widget_timer_has_ended to
                R.string.the_timer_for_your_widget_has_ended_tap_to_view_the_results_and_see_how_your_audience_responded

        }

        createNotificationChannel(
            applicationContext,
            applicationContext.getString(title),
            applicationContext.getString(message),
            notificationType.name,
            when (notificationType) {
                NotificationType.SYNC_DATA -> NotificationManager.IMPORTANCE_LOW
                else -> NotificationManager.IMPORTANCE_HIGH
            }
        )
        val builder = NotificationCompat.Builder(applicationContext, notificationType.name)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle(applicationContext.getString(title))
            .setContentText(applicationContext.getString(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)


        if (progress != null) {
            if (progress == -1f) {
                builder.setProgress(0, 0, true)
            } else {
                builder.setProgress(100, (progress * 100).toInt(), false)
            }
        }
        if (notificationType == NotificationType.SYNC_DATA) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
            builder.setSilent(true)
        }


        notificationManager.notify(
            notificationType.ordinal,
            builder.build()
        )
    }

    private fun createNotificationChannel(
        context: Context,
        title: String,
        description: String,
        channelId: String,
        importance: Int = NotificationManager.IMPORTANCE_HIGH
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, title, importance).apply {
                setDescription(description)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelNotification(applicationContext: Context, notificationType: NotificationType) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.cancel(notificationType.ordinal)
    }

}
