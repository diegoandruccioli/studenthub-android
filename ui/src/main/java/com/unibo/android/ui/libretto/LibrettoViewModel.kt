package com.unibo.android.ui.libretto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.usecase.AddEsameUseCase
import com.unibo.android.domain.usecase.DeleteEsameUseCase
import com.unibo.android.domain.usecase.GetEsamiUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibrettoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepositoryProvider).getEsameRepository()
    private val getEsamiUseCase = GetEsamiUseCase(repository)
    private val addEsameUseCase = AddEsameUseCase(repository)
    private val deleteEsameUseCase = DeleteEsameUseCase(repository)

    private val _esami = MutableStateFlow<List<Esame>>(emptyList())
    val esami: StateFlow<List<Esame>> = _esami.asStateFlow()

    init {
        viewModelScope.launch {
            getEsamiUseCase().collect { lista ->
                _esami.value = lista
            }
        }
    }

    fun addEsame(esame: Esame) {
        viewModelScope.launch { addEsameUseCase(esame) }
    }

    fun deleteEsame(esame: Esame) {
        viewModelScope.launch { deleteEsameUseCase(esame) }
    }
}
