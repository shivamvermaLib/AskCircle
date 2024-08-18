package com.ask.user

import com.ask.core.DISPATCHER_DEFAULT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class GoogleLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(idToken: String) = withContext(dispatcher) {
        userRepository.googleLogin(idToken)
    }
}