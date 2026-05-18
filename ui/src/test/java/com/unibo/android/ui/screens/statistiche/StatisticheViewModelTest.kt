package com.unibo.android.ui.screens.statistiche

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.usecase.GetStatisticheUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticheViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val useCase: GetStatisticheUseCase = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stato iniziale Loading`() = runTest {
        whenever(useCase()).thenReturn(flowOf(Result.success(statisticheVuote())))

        val vm = StatisticheViewModel(useCase, Locale.ITALY)

        assertEquals(StatisticheUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `nessun esame - stato diventa Empty`() = runTest {
        whenever(useCase()).thenReturn(flowOf(Result.success(statisticheVuote())))

        val vm = StatisticheViewModel(useCase, Locale.ITALY)
        advanceUntilIdle()

        assertEquals(StatisticheUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `esami presenti - stato diventa Success con dati corretti`() = runTest {
        val stats = Statistiche(
            mediaPonderata = 27.5,
            cfuSostenuti = 12,
            baseLaurea = 100.83,
            andamentoCarriera = listOf(
                PuntoAndamento(LocalDate.of(2025, 1, 10), voto = 25, mediaPonderataProgressiva = 25.0),
                PuntoAndamento(LocalDate.of(2025, 2, 15), voto = 30, mediaPonderataProgressiva = 27.5)
            )
        )
        whenever(useCase()).thenReturn(flowOf(Result.success(stats)))

        val vm = StatisticheViewModel(useCase, Locale.ITALY)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is StatisticheUiState.Success)
        val uiModel = (state as StatisticheUiState.Success).uiModel
        assertEquals("27,5", uiModel.mediaPonderata)
        assertEquals("12", uiModel.cfuSostenuti)
    }

    @Test
    fun `errore dal use case - stato diventa Error`() = runTest {
        whenever(useCase()).thenReturn(flowOf(Result.failure(Exception("Errore DB"))))

        val vm = StatisticheViewModel(useCase, Locale.ITALY)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is StatisticheUiState.Error)
        assertEquals("Errore DB", (state as StatisticheUiState.Error).message)
    }

    private fun statisticheVuote() = Statistiche(0.0, 0, 0.0, emptyList())
}
