package com.ask.common

import com.ask.core.DISPATCHER_DEFAULT
import com.ask.core.badwords.BadWordRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class CheckModerationUseCase @Inject constructor(
    private val badWordRepository: BadWordRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(sentence: String): Boolean = withContext(dispatcher) {
        badWordRepository.checkIfBadWordExists(sentence)
    }

}