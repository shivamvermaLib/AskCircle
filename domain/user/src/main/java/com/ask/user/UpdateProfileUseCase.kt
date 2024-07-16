package com.ask.user

import com.ask.analytics.AnalyticsLogger
import com.ask.core.checkIfUrl
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val analyticsLogger: AnalyticsLogger
) {

    suspend operator fun invoke(
        name: String,
        email: String,
        gender: Gender?,
        age: Int?,
        profilePic: String?,
        country: String?,
        getExtension: (String) -> String?,
        getBytes: (String) -> ByteArray?,
        preloadImage: suspend (String) -> Unit
    ) {
        analyticsLogger.updateProfileEvent(
            gender,
            age,
            country,
            profilePic.isNullOrBlank().not(),
            email.isNotBlank()
        )
        val extension = profilePic?.let { path ->
            path.takeIf { it.isNotBlank() && it.checkIfUrl().not() }
                ?.let { getExtension(it) }
        }
        userRepository.updateUser(
            name = name,
            email = email,
            gender = gender,
            age = age,
            country = country,
            profilePicExtension = extension,
            profileByteArray = profilePic?.takeIf { it.checkIfUrl().not() }
                ?.let { path -> getBytes(path) },
        ).also {
            it.user.profilePic?.let { it1 -> preloadImage(it1) }
        }.also {
            analyticsLogger.profileUpdatedEvent(
                gender,
                age,
                country,
                profilePic.isNullOrBlank().not(),
                email.isNotBlank()
            )
        }
    }
}