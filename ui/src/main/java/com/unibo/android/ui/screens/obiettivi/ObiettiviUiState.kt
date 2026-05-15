package com.unibo.android.ui.screens.obiettivi

import com.unibo.android.domain.model.Obiettivo

sealed class ObiettiviUiState {
    object Loading : ObiettiviUiState()
    data class Success(val obiettivi: List<Obiettivo>) : ObiettiviUiState()
    data class Error(val message: String) : ObiettiviUiState()
}
