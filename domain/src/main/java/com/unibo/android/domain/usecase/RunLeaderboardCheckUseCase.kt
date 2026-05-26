package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.GamificationRepository

class RunLeaderboardCheckUseCase(private val repository: GamificationRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.runLeaderboardWorkerNow()
}
