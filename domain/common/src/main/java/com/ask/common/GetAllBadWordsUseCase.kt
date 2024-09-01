package com.ask.common

import com.ask.core.DISPATCHER_DEFAULT
import com.ask.core.badwords.BadWordRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Named

class GetAllBadWordsUseCase @Inject constructor(
    private val badWordRepository: BadWordRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    operator fun invoke() = badWordRepository.getAllBadWordsFlow()

}