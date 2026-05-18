package com.unibo.android.ui.screens.profilo

import com.unibo.android.domain.model.Settings

sealed class ProfiloUiState {
    object Loading : ProfiloUiState()
    data class Success(val settings: Settings) : ProfiloUiState()
    data class Saving(val previousSettings: Settings) : ProfiloUiState()
    data class Error(val message: String) : ProfiloUiState()
}
