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
            
            // Carica statistiche e leaderboard in parallelo
            val statsResult = useCase.getUserStats()
            val leaderboardResult = useCase.getLeaderboard()

            // Verifica se entrambe le operazioni sono riuscite
            if (statsResult.isSuccess && leaderboardResult.isSuccess) {
                val userStats = statsResult.getOrNull()
                val leaderboard = leaderboardResult.getOrNull()
                
                if (userStats != null && leaderboard != null) {
                    _uiState.value = GamificationUiState.Success(
                        userStats = userStats,
                        leaderboard = leaderboard
                    )
                } else {
                    _uiState.value = GamificationUiState.Error(
                        "Dati incompleti: statistiche o leaderboard vuoti"
                    )
                }
            } else {
                // Raccogli il messaggio di errore dal risultato fallito
                val errorMessage = statsResult.exceptionOrNull()?.message 
                    ?: leaderboardResult.exceptionOrNull()?.message 
                    ?: "Errore sconosciuto nel caricamento dei dati di gamification"
                    
                _uiState.value = GamificationUiState.Error(errorMessage)
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
