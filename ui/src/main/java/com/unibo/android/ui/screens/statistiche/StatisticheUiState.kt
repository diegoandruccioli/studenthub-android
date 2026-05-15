package com.unibo.android.ui.screens.statistiche

import com.unibo.android.domain.model.Statistiche

sealed interface StatisticheUiState {
    object Loading : StatisticheUiState
    object Empty : StatisticheUiState
    data class Success(val stats: Statistiche) : StatisticheUiState
    data class Error(val message: String) : StatisticheUiState
}
