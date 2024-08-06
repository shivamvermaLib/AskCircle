package com.ask.common

import com.ask.core.AppSharedPreference
import com.ask.core.DISPATCHER_DEFAULT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class GetLastSearchDataUseCase @Inject constructor(
    private val sharedPreference: AppSharedPreference,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke() = withContext(dispatcher) {
        sharedPreference.getAiSearchPrompt()
    }
}