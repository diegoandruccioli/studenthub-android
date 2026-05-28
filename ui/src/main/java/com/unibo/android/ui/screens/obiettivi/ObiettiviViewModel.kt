package com.unibo.android.ui.screens.obiettivi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.usecase.GetObiettiviUseCase
import com.unibo.android.domain.usecase.RefreshObiettiviUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ObiettiviViewModel(
    private val getObiettiviUseCase: GetObiettiviUseCase,
    private val refreshObiettiviUseCase: RefreshObiettiviUseCase,
) : ViewModel() {
    val uiState: StateFlow<ObiettiviUiState> =
        getObiettiviUseCase()
            .map<List<Obiettivo>, ObiettiviUiState> { ObiettiviUiState.Success(it) }
            .catch { e -> emit(ObiettiviUiState.Error(e.message ?: "Errore di sistema")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ObiettiviUiState.Loading)

    fun refreshObiettivi() {
        viewModelScope.launch {
            refreshObiettiviUseCase()
        }
    }

    companion object {
        fun provideFactory(
            getObiettiviUseCase: GetObiettiviUseCase,
            refreshObiettiviUseCase: RefreshObiettiviUseCase,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ObiettiviViewModel(getObiettiviUseCase, refreshObiettiviUseCase) as T
                }
            }
    }
}
