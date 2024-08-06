package com.ask.widget

import com.ask.core.DISPATCHER_DEFAULT
import com.ask.user.User
import com.ask.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class BookmarkWidgetUseCase @Inject constructor(
    private val userRepository: UserRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(widgetId: String) = withContext(dispatcher) {
        val user = userRepository.getCurrentUserOptional()!!
        val widgetBookmarks = user.userWidgetBookmarks.toMutableList()
        if (widgetBookmarks.removeIf { it.widgetId == widgetId }.not()) {
            widgetBookmarks.add(
                User.UserWidgetBookmarks(
                    widgetId = widgetId
                )
            )
        }
        userRepository.updateUser(widgetBookmarks = widgetBookmarks)
    }
}