package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.GamificationRepository

/**
 * Use case to refresh the global leaderboard from the remote server.
 * This triggers a network call and updates the local Room database (SSOT).
 */
class RefreshLeaderboardUseCase(private val repository: GamificationRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.getLeaderboard()
}
