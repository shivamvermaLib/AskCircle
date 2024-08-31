package com.ask.common

import com.ask.core.RemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckMaintenanceModeUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
) {

    operator fun invoke(): Flow<Boolean> =
        remoteConfigRepository.fetchLiveInit(RemoteConfigRepository.MAINTENANCE_MODE)
            .map { remoteConfigRepository.maintenanceMode() }
}