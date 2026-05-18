package com.unibo.android.ui.screens.obiettivi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.usecase.GetObiettiviUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ObiettiviViewModel(
    private val getObiettiviUseCase: GetObiettiviUseCase
) : ViewModel() {

    val uiState: StateFlow<ObiettiviUiState> = getObiettiviUseCase()
        .map { lista ->
            ObiettiviUiState.Success(lista) as ObiettiviUiState
        }
        .catch { e ->
            emit(ObiettiviUiState.Error(e.message ?: "Errore di sistema"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ObiettiviUiState.Loading
        )

    companion object {
        fun provideFactory(
            getObiettiviUseCase: GetObiettiviUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ObiettiviViewModel(getObiettiviUseCase) as T
            }
        }
    }
}
