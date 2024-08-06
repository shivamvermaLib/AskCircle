package com.ask.widget

import com.ask.analytics.AnalyticsLogger
import com.ask.core.AppSharedPreference
import com.ask.core.ImageSizeType
import com.ask.user.UserRepository
import javax.inject.Inject

class CreateWidgetUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val analyticsLogger: AnalyticsLogger,
    private val sharedPreference: AppSharedPreference
) {

    private fun createWidgetEvent(w: WidgetWithOptionsAndVotesForTargetAudience) {
        analyticsLogger.createWidgetEvent(
            w.widget.widgetType,
            w.widget.description.isNullOrBlank().not(),
            w.options.size,
            w.options.all { it.option.imageUrl != null },
            w.targetAudienceGender.gender,
            w.targetAudienceLocations.mapNotNull { it.country },
            w.targetAudienceAgeRange.min,
            w.targetAudienceAgeRange.max
        )
    }

    private fun createdWidgetEvent(w: WidgetWithOptionsAndVotesForTargetAudience) {
        analyticsLogger.createdWidgetEvent(
            w.widget.id,
            w.widget.widgetType,
            w.widget.description.isNullOrBlank().not(),
            w.options.size,
            w.options.all { it.option.imageUrl != null },
            w.targetAudienceGender.gender,
            w.targetAudienceLocations.mapNotNull { it.country },
            w.targetAudienceAgeRange.min,
            w.targetAudienceAgeRange.max
        )
    }

    suspend operator fun invoke(
        widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
        getExtension: (String) -> String,
        getByteArray: suspend (String) -> Map<ImageSizeType, ByteArray>,
        preloadImages: suspend (List<String>) -> Unit
    ): WidgetWithOptionsAndVotesForTargetAudience {
        createWidgetEvent(widgetWithOptionsAndVotesForTargetAudience)
        return widgetRepository.createWidget(
            widgetWithOptionsAndVotesForTargetAudience.copy(
                widget = widgetWithOptionsAndVotesForTargetAudience.widget.copy(
                    creatorId = userRepository.getCurrentUserId()
                )
            ),
            userRepository.getCurrentUserId(),
            getExtension,
            getByteArray,
            preloadImages
        ).also {
            val lastUpdatedTime = sharedPreference.getUpdatedTime()
            sharedPreference.setUpdatedTime(
                lastUpdatedTime.copy(
                    widgetTime = System.currentTimeMillis(),
                )
            )
            createdWidgetEvent(it)
        }
    }
}