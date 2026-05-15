package com.unibo.android.ui.screens.statistiche

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.usecase.GetStatisticheUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale

class StatisticheViewModel(
    private val getStatisticheUseCase: GetStatisticheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticheUiState>(StatisticheUiState.Loading)
    val uiState: StateFlow<StatisticheUiState> = _uiState.asStateFlow()

    init {
        loadStatistiche()
    }

    private fun loadStatistiche() {
        viewModelScope.launch {
            getStatisticheUseCase()
                .catch { e ->
                    _uiState.value = StatisticheUiState.Error(e.message ?: "Errore sconosciuto")
                }
                .collect { stats ->
                    _uiState.value = if (stats.cfuSostenuti == 0) {
                        StatisticheUiState.Empty
                    } else {
                        StatisticheUiState.Success(toUiModel(stats))
                    }
                }
        }
    }

    /**
     * Trasforma il modello di dominio in un modello di visualizzazione (UI Model).
     * Qui avviene la proiezione: formattazione stringhe e normalizzazione punti grafico (0.0 - 1.0).
     * La logica di "business grafico" (range 18-30) muore qui e non entra nella View.
     */
    private fun toUiModel(stats: Statistiche): StatisticheUiModel {
        val minVoto = 18f
        val maxVoto = 30f
        val range = maxVoto - minVoto

        val numPunti = stats.andamentoCarriera.size
        val indexStep = if (numPunti > 1) 1f / (numPunti - 1) else 0.5f

        val puntiVoti = stats.andamentoCarriera.mapIndexed { index, punto ->
            OffsetRelativo(
                x = index * indexStep,
                y = (punto.voto.toFloat() - minVoto) / range
            )
        }

        val puntiMedia = stats.andamentoCarriera.mapIndexed { index, punto ->
            OffsetRelativo(
                x = index * indexStep,
                y = (punto.mediaPonderataProgressiva.toFloat() - minVoto) / range
            )
        }

        return StatisticheUiModel(
            mediaPonderata = String.format(Locale.ITALY, "%.1f", stats.mediaPonderata),
            cfuSostenuti = stats.cfuSostenuti.toString(),
            baseLaurea = String.format(Locale.ITALY, "%.1f", stats.baseLaurea),
            puntiVoti = puntiVoti,
            puntiMedia = puntiMedia
        )
    }

    companion object {
        fun provideFactory(getStatisticheUseCase: GetStatisticheUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatisticheViewModel(getStatisticheUseCase) as T
                }
            }
    }
}
