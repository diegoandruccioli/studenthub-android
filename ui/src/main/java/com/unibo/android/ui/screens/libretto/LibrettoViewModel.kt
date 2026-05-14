package com.unibo.android.ui.screens.libretto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.usecase.AddEsameUseCase
import com.unibo.android.domain.usecase.CheckObiettiviUseCase
import com.unibo.android.domain.usecase.DeleteEsameUseCase
import com.unibo.android.domain.usecase.GetEsamiUseCase
import com.unibo.android.domain.usecase.RefreshEsamiUseCase
import com.unibo.android.domain.usecase.UpdateEsameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortBy { DATA, VOTO, CFU }
enum class SortOrder { ASC, DESC }

class LibrettoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepositoryProvider).getEsameRepository()
    private val obiettivoRepository = (application as RepositoryProvider).getObiettivoRepository()
    private val getEsamiUseCase = GetEsamiUseCase(repository)
    private val addEsameUseCase = AddEsameUseCase(repository)
    private val updateEsameUseCase = UpdateEsameUseCase(repository)
    private val deleteEsameUseCase = DeleteEsameUseCase(repository)
    private val checkObiettiviUseCase = CheckObiettiviUseCase(obiettivoRepository)
    private val refreshEsamiUseCase = RefreshEsamiUseCase(repository)

    private val _sortBy = MutableStateFlow(SortBy.DATA)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val esami: StateFlow<List<Esame>> = combine(
        getEsamiUseCase(),
        _sortBy,
        _sortOrder
    ) { lista, by, order ->
        val sorted = when (by) {
            SortBy.DATA -> lista.sortedBy { it.dataEsame.toSortableDate() }
            SortBy.VOTO -> lista.sortedBy { it.voto }
            SortBy.CFU -> lista.sortedBy { it.cfu }
        }
        if (order == SortOrder.DESC) sorted.reversed() else sorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { refreshEsamiUseCase() }
    }

    fun setSortBy(sortBy: SortBy) { _sortBy.value = sortBy }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
    }

    fun addEsame(esame: Esame) {
        viewModelScope.launch {
            addEsameUseCase(esame)
            checkObiettiviUseCase()
        }
    }

    fun updateEsame(esame: Esame) {
        viewModelScope.launch { updateEsameUseCase(esame) }
    }

    fun deleteEsame(esame: Esame) {
        viewModelScope.launch {
            deleteEsameUseCase(esame)
            checkObiettiviUseCase()
        }
    }

    private fun String.toSortableDate(): String {
        val parts = split("/")
        return if (parts.size == 3) "${parts[2]}${parts[1].padStart(2,'0')}${parts[0].padStart(2,'0')}"
        else this
    }
}
