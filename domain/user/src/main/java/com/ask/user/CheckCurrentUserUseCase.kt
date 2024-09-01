package com.ask.user

import javax.inject.Inject

class CheckCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): UserWithLocationCategory? {
        return userRepository.checkCurrentUser()
    }
}

