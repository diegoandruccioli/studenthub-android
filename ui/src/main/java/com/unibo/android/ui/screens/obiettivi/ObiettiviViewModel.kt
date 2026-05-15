package com.unibo.android.ui.screens.obiettivi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.usecase.GetObiettiviUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ObiettiviViewModel(
    private val getObiettiviUseCase: GetObiettiviUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ObiettiviUiState>(ObiettiviUiState.Loading)
    val uiState: StateFlow<ObiettiviUiState> = _uiState.asStateFlow()

    init {
        loadObiettivi()
    }

    private fun loadObiettivi() {
        viewModelScope.launch {
            getObiettiviUseCase()
                .catch { e ->
                    _uiState.value = ObiettiviUiState.Error(e.message ?: "Errore di sistema")
                }
                .collect { lista ->
                    _uiState.value = ObiettiviUiState.Success(lista)
                }
        }
    }

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
