package com.unibo.android.ui.screens.obiettivi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.usecase.GetObiettiviUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ObiettiviViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepositoryProvider).getObiettivoRepository()
    private val getObiettiviUseCase = GetObiettiviUseCase(repository)

    private val _obiettivi = MutableStateFlow<List<Obiettivo>>(emptyList())
    val obiettivi: StateFlow<List<Obiettivo>> = _obiettivi.asStateFlow()

    init {
        viewModelScope.launch {
            getObiettiviUseCase().collect { lista ->
                _obiettivi.value = lista
            }
        }
    }
}
