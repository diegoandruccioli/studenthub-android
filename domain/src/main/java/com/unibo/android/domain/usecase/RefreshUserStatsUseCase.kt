package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.GamificationRepository

/**
 * Use case to refresh user statistics from the remote server.
 * This triggers a network call and updates the local DataStore.
 */
class RefreshUserStatsUseCase(private val repository: GamificationRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.getUserStats()
}
