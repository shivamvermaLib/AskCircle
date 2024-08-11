package com.ask.user

import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke(idToken: String?, email: String?) {
        userRepository.signInWithGoogle(idToken, email)
    }

}