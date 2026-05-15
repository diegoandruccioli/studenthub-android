package com.unibo.android.ui.screens.statistiche

sealed interface StatisticheUiState {
    object Loading : StatisticheUiState
    object Empty : StatisticheUiState
    data class Success(val uiModel: StatisticheUiModel) : StatisticheUiState
    data class Error(val message: String) : StatisticheUiState
}
