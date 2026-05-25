package com.unibo.android.ui.screens.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.usecase.GetGamificationDataUseCase
import com.unibo.android.domain.usecase.GetLeaderboardUseCase
import com.unibo.android.domain.usecase.RefreshLeaderboardUseCase
import com.unibo.android.domain.usecase.RefreshUserStatsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Gamification screen.
 * Exposes a single [GamificationUiState] following the UDF pattern.
 */
class GamificationViewModel(
    private val getGamificationDataUseCase: GetGamificationDataUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val refreshUserStatsUseCase: RefreshUserStatsUseCase,
    private val refreshLeaderboardUseCase: RefreshLeaderboardUseCase,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    /**
     * The unified UI state for the screen.
     */
    val uiState: StateFlow<GamificationUiState> = combine(
        getGamificationDataUseCase(),
        getLeaderboardUseCase(),
        _errorMessage,
        _isLoading,
    ) { stats, leaderboard, error, isLoading ->
        when {
            error != null -> GamificationUiState.Error(error)
            isLoading -> GamificationUiState.Loading
            else -> GamificationUiState.Success(
                stats = stats,
                leaderboard = leaderboard.asSequence()
                    .sortedByDescending { it.xpTotali }
                    .map { it.copy(isMe = it.userId == stats.userId) }
                    .toList(),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GamificationUiState.Loading)

    init {
        refreshAll()
    }

    /**
     * Refreshes both leaderboard and user statistics.
     */
    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val leaderboardDeferred = async { refreshLeaderboardUseCase() }
            val statsDeferred = async { refreshUserStatsUseCase() }

            val leaderboardResult = leaderboardDeferred.await()
            val statsResult = statsDeferred.await()

            if (leaderboardResult.isFailure || statsResult.isFailure) {
                _errorMessage.value = "Impossibile caricare i dati della gamification"
            }
            _isLoading.value = false
        }
    }

    /**
     * Refreshes only the leaderboard.
     */
    fun refreshLeaderboard() {
        viewModelScope.launch {
            refreshLeaderboardUseCase()
                .onFailure { _errorMessage.value = "Impossibile aggiornare la classifica" }
        }
    }

    /**
     * Refreshes only the user statistics from the network.
     */
    fun refreshStats() {
        viewModelScope.launch {
            refreshUserStatsUseCase()
                .onFailure { _errorMessage.value = "Impossibile aggiornare le statistiche" }
        }
    }

    companion object {
        fun provideFactory(
            gamificationRepository: GamificationRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GamificationViewModel(
                        GetGamificationDataUseCase(gamificationRepository),
                        GetLeaderboardUseCase(gamificationRepository),
                        RefreshUserStatsUseCase(gamificationRepository),
                        RefreshLeaderboardUseCase(gamificationRepository),
                    ) as T
                }
            }
    }
}
