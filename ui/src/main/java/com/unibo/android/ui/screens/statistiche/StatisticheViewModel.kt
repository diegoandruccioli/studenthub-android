package com.unibo.android.ui.screens.statistiche

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.usecase.GetStatisticheUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticheViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepositoryProvider).getEsameRepository()
    private val getStatisticheUseCase = GetStatisticheUseCase(repository)

    private val _uiState = MutableStateFlow<Statistiche?>(null)
    val uiState: StateFlow<Statistiche?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getStatisticheUseCase().collect { stats ->
                _uiState.value = stats
            }
        }
    }
}
