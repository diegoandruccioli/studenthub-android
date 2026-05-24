package com.unibo.android.ui.screens.libretto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.usecase.AddEsameUseCase
import com.unibo.android.domain.usecase.DeleteEsameUseCase
import com.unibo.android.domain.usecase.GetEsamiUseCase
import com.unibo.android.domain.usecase.RefreshEsamiUseCase
import com.unibo.android.domain.usecase.RefreshObiettiviUseCase
import com.unibo.android.domain.usecase.RefreshUserStatsUseCase
import com.unibo.android.domain.usecase.UpdateEsameUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortBy { DATA, VOTO, CFU }
enum class SortOrder { ASC, DESC }

class LibrettoViewModel(
    private val getEsamiUseCase: GetEsamiUseCase,
    private val addEsameUseCase: AddEsameUseCase,
    private val updateEsameUseCase: UpdateEsameUseCase,
    private val deleteEsameUseCase: DeleteEsameUseCase,
    private val refreshEsamiUseCase: RefreshEsamiUseCase,
    private val refreshUserStatsUseCase: RefreshUserStatsUseCase,
    private val refreshObiettiviUseCase: RefreshObiettiviUseCase,
) : ViewModel() {

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
            SortBy.DATA -> lista.sortedBy { it.dataEsame }
            SortBy.VOTO -> lista.sortedBy { it.voto }
            SortBy.CFU -> lista.sortedBy { it.cfu }
        }
        if (order == SortOrder.DESC) sorted.reversed() else sorted
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { refreshEsamiUseCase() }
    }

    fun setSortBy(sortBy: SortBy) { _sortBy.value = sortBy }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
    }

    fun addEsame(esame: Esame) {
        viewModelScope.launch {
            val result = addEsameUseCase(esame)
            if (result.isSuccess) {
                // Aggiorniamo sia statistiche che obiettivi in parallelo per reattività massima
                async { refreshUserStatsUseCase() }
                async { refreshObiettiviUseCase() }
            }
        }
    }

    fun updateEsame(esame: Esame) {
        viewModelScope.launch {
            val result = updateEsameUseCase(esame)
            if (result.isSuccess) {
                async { refreshUserStatsUseCase() }
                async { refreshObiettiviUseCase() }
            }
        }
    }

    fun deleteEsame(esame: Esame) {
        viewModelScope.launch {
            val result = deleteEsameUseCase(esame)
            if (result.isSuccess) {
                async { refreshUserStatsUseCase() }
                async { refreshObiettiviUseCase() }
            }
        }
    }

    companion object {
        fun provideFactory(
            esameRepository: EsameRepository,
            gamificationRepository: GamificationRepository,
            obiettivoRepository: ObiettivoRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LibrettoViewModel(
                getEsamiUseCase = GetEsamiUseCase(esameRepository),
                addEsameUseCase = AddEsameUseCase(esameRepository),
                updateEsameUseCase = UpdateEsameUseCase(esameRepository),
                deleteEsameUseCase = DeleteEsameUseCase(esameRepository),
                refreshEsamiUseCase = RefreshEsamiUseCase(esameRepository),
                refreshUserStatsUseCase = RefreshUserStatsUseCase(gamificationRepository),
                refreshObiettiviUseCase = RefreshObiettiviUseCase(obiettivoRepository)
            ) as T
        }
    }
}
