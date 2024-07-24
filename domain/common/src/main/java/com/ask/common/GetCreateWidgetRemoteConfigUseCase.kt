package com.ask.common

import com.ask.core.CreateRemoteConfig
import com.ask.core.RemoteConfigRepository
import javax.inject.Inject

class GetCreateWidgetRemoteConfigUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
) {
    operator fun invoke(): CreateRemoteConfig {
        return CreateRemoteConfig(
            remoteConfigRepository.getAgeRangeMin().toInt(),
            remoteConfigRepository.getAgeRangeMax().toInt(),
            remoteConfigRepository.getMaxYearAllowed().toInt(),
            remoteConfigRepository.getMaxOptionSize().toInt()
        )
    }
}

