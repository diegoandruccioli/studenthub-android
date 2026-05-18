package com.unibo.android.ui.screens.statistiche

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.usecase.GetStatisticheUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class StatisticheViewModel(
    private val getStatisticheUseCase: GetStatisticheUseCase,
    private val locale: Locale = Locale.getDefault()
) : ViewModel() {

    val uiState: StateFlow<StatisticheUiState> = getStatisticheUseCase()
        .map { result ->
            result.fold(
                onSuccess = { stats ->
                    if (stats.cfuSostenuti == 0) {
                        StatisticheUiState.Empty
                    } else {
                        StatisticheUiState.Success(toUiModel(stats))
                    }
                },
                onFailure = { error ->
                    StatisticheUiState.Error(error.message ?: "Errore integrità dati")
                }
            )
        }
        .catch { e ->
            emit(StatisticheUiState.Error(e.message ?: "Errore di sistema"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticheUiState.Loading
        )

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
