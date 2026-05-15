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
    private val getStatisticheUseCase: GetStatisticheUseCase,
    private val locale: Locale = Locale.getDefault()
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
                    _uiState.value = StatisticheUiState.Error(e.message ?: "Errore di sistema")
                }
                .collect { result ->
                    result.onSuccess { stats ->
                        _uiState.value = if (stats.cfuSostenuti == 0) {
                            StatisticheUiState.Empty
                        } else {
                            StatisticheUiState.Success(toUiModel(stats))
                        }
                    }.onFailure { error ->
                        _uiState.value = StatisticheUiState.Error(error.message ?: "Errore integrità dati")
                    }
                }
        }
    }

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

        // Utilizzo del Locale iniettato per rispettare i principi di internazionalizzazione
        val yMediaFissa = (stats.mediaPonderata.toFloat() - minVoto) / range

        return StatisticheUiModel(
            mediaPonderata = String.format(locale, "%.1f", stats.mediaPonderata),
            cfuSostenuti = stats.cfuSostenuti.toString(),
            baseLaurea = String.format(locale, "%.1f", stats.baseLaurea),
            puntiVoti = puntiVoti,
            puntiMedia = puntiMedia,
            yMediaFissa = yMediaFissa.coerceIn(0f, 1f)
        )
    }

    companion object {
        fun provideFactory(
            getStatisticheUseCase: GetStatisticheUseCase,
            locale: Locale = Locale.getDefault()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatisticheViewModel(getStatisticheUseCase, locale) as T
                }
            }
    }
}
