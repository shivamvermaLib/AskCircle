package com.ask.common

import com.ask.core.AgeRange
import com.ask.core.RemoteConfigRepository
import javax.inject.Inject

class GetAgeRemoteConfigUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
) {
    operator fun invoke(): AgeRange {
        return AgeRange(
            remoteConfigRepository.getAgeRangeMin().toInt(),
            remoteConfigRepository.getAgeRangeMax().toInt()
        )
    }
}

