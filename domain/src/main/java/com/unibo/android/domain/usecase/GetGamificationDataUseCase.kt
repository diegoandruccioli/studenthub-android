package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow

class GetGamificationDataUseCase(private val repository: GamificationRepository) {
    operator fun invoke(): Flow<UserStats> = repository.userStatsFlow
}
