package com.unibo.android.ui.screens.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.usecase.GetGamificationDataUseCase
import com.unibo.android.domain.usecase.GetLeaderboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GamificationViewModel(
    private val getGamificationDataUseCase: GetGamificationDataUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    val userStats: StateFlow<UserStats?> = getGamificationDataUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = combine(
        _leaderboard,
        userStats
    ) { entries, stats ->
        entries.sortedByDescending { it.xpTotali }
            .map { it.copy(isMe = it.userId == stats?.userId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshLeaderboard()
    }

    fun refreshLeaderboard() {
        viewModelScope.launch {
            getLeaderboardUseCase().onSuccess {
                _leaderboard.value = it
            }
        }
    }

    companion object {
        fun provideFactory(repository: GamificationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GamificationViewModel(
                        GetGamificationDataUseCase(repository),
                        GetLeaderboardUseCase(repository)
                    ) as T
                }
            }
    }
}
