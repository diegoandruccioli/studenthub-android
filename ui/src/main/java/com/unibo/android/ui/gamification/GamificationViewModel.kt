package com.unibo.android.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.usecase.GetGamificationDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamificationViewModel(
    private val useCase: GetGamificationDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GamificationUiState>(GamificationUiState.Loading)
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = GamificationUiState.Loading
            
            val statsResult = useCase.getUserStats()
            val leaderboardResult = useCase.getLeaderboard()

            if (statsResult.isSuccess && leaderboardResult.isSuccess) {
                _uiState.value = GamificationUiState.Success(
                    userStats = statsResult.getOrNull()!!,
                    leaderboard = leaderboardResult.getOrNull()!!
                )
            } else {
                _uiState.value = GamificationUiState.Error("Errore nel caricamento dei dati di gamification")
            }
        }
    }

    companion object {
        fun provideFactory(
            useCase: GetGamificationDataUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GamificationViewModel(useCase) as T
            }
        }
    }
}
