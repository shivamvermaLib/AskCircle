package com.ask.common

import com.ask.core.RemoteConfigRepository
import javax.inject.Inject

class GetMaxOptionRemoteConfigUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
) {
    operator fun invoke(): Int {
        return remoteConfigRepository.getMaxOptionSize().toInt()
    }

}