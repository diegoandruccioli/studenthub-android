package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.utils.GamificationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetGamificationDataUseCase(
    private val gamificationRepository: GamificationRepository,
    private val esameRepository: EsameRepository
) {
    operator fun invoke(): Flow<UserStats> = combine(
        gamificationRepository.userStatsFlow,
        esameRepository.totalXpFlow
    ) { remoteStats, localXp ->
        // Calcoliamo il livello e il progresso basandoci sugli XP locali (Optimistic SSOT)
        val level = GamificationUtils.calculateLevel(localXp)
        val xpInLevel = GamificationUtils.getXpInCurrentLevel(localXp)
        
        remoteStats.copy(
            xp = localXp,
            level = level,
            levelTitle = "Lv. $level - ${GamificationUtils.getLevelTitle(level)}",
            progressPercentage = xpInLevel.toFloat() / GamificationUtils.XP_PER_LEVEL.toFloat(),
            xpLabel = "$xpInLevel / ${GamificationUtils.XP_PER_LEVEL} XP"
        )
    }
}
