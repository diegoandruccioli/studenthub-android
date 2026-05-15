package com.unibo.android.ui.screens.statistiche

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.usecase.GetStatisticheUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StatisticheViewModel(
    private val getStatisticheUseCase: GetStatisticheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticheUiState>(StatisticheUiState.Loading)
    val uiState: StateFlow<StatisticheUiState> = _uiState.asStateFlow()

    init {
        loadStatistiche()
    }

    private fun loadStatistiche() {
        viewModelScope.launch {
            getStatisticheUseCase()
                .catch { e ->
                    _uiState.value = StatisticheUiState.Error(e.message ?: "Errore sconosciuto")
                }
                .collect { stats ->
                    _uiState.value = if (stats.cfuSostenuti == 0) {
                        StatisticheUiState.Empty
                    } else {
                        StatisticheUiState.Success(stats)
                    }
                }
        }
    }

    /**
     * Factory per iniettare le dipendenze senza AndroidViewModel.
     * In un progetto reale si userebbe Hilt, ma qui seguiamo la DI manuale del corso.
     */
    companion object {
        fun provideFactory(getStatisticheUseCase: GetStatisticheUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatisticheViewModel(getStatisticheUseCase) as T
                }
            }
    }
}
