package com.ask.user

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Flow<UserWithLocation> {
        return userRepository.getCurrentUserLive()
    }

}