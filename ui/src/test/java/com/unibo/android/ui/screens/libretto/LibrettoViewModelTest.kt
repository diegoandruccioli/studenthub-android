package com.unibo.android.ui.screens.libretto

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.usecase.AddEsameUseCase
import com.unibo.android.domain.usecase.CheckObiettiviUseCase
import com.unibo.android.domain.usecase.DeleteEsameUseCase
import com.unibo.android.domain.usecase.GetEsamiUseCase
import com.unibo.android.domain.usecase.RefreshEsamiUseCase
import com.unibo.android.domain.usecase.UpdateEsameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LibrettoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getEsami: GetEsamiUseCase = mock()
    private val addEsame: AddEsameUseCase = mock()
    private val updateEsame: UpdateEsameUseCase = mock()
    private val deleteEsame: DeleteEsameUseCase = mock()
    private val checkObiettivi: CheckObiettiviUseCase = mock()
    private val refreshEsami: RefreshEsamiUseCase = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun makeVm(): LibrettoViewModel {
        whenever(getEsami()).thenReturn(flowOf(esami))
        return LibrettoViewModel(getEsami, addEsame, updateEsame, deleteEsame, checkObiettivi, refreshEsami)
    }

    private val esame1 = Esame(id = 1, nome = "Analisi", voto = 28, lode = false, cfu = 6, dataEsame = LocalDate.of(2025, 1, 10))
    private val esame2 = Esame(id = 2, nome = "Fisica", voto = 24, lode = false, cfu = 12, dataEsame = LocalDate.of(2025, 3, 15))
    private val esame3 = Esame(id = 3, nome = "Algebra", voto = 30, lode = true, cfu = 9, dataEsame = LocalDate.of(2025, 2, 5))
    private val esami = listOf(esame1, esame2, esame3)

    @Test
    fun `stato iniziale - ordine default per data DESC`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        val lista = vm.esami.first()
        assertEquals(esame2.id, lista[0].id) // 2025-03-15 più recente
        assertEquals(esame3.id, lista[1].id) // 2025-02-05
        assertEquals(esame1.id, lista[2].id) // 2025-01-10
    }

    @Test
    fun `setSortBy VOTO - ordina per voto DESC`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.setSortBy(SortBy.VOTO)
        advanceUntilIdle()

        val lista = vm.esami.first()
        assertEquals(esame3.id, lista[0].id) // voto 30
        assertEquals(esame1.id, lista[1].id) // voto 28
        assertEquals(esame2.id, lista[2].id) // voto 24
    }

    @Test
    fun `setSortBy CFU - ordina per CFU DESC`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.setSortBy(SortBy.CFU)
        advanceUntilIdle()

        val lista = vm.esami.first()
        assertEquals(esame2.id, lista[0].id) // CFU 12
        assertEquals(esame3.id, lista[1].id) // CFU 9
        assertEquals(esame1.id, lista[2].id) // CFU 6
    }

    @Test
    fun `toggleSortOrder - inverte da DESC ad ASC`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.toggleSortOrder()
        advanceUntilIdle()

        assertEquals(SortOrder.ASC, vm.sortOrder.value)
        val lista = vm.esami.first()
        assertEquals(esame1.id, lista[0].id) // 2025-01-10 meno recente
    }

    @Test
    fun `addEsame - chiama checkObiettivi dopo aggiunta`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.addEsame(esame1)
        advanceUntilIdle()

        verify(addEsame)(esame1)
        verify(checkObiettivi)()
    }

    @Test
    fun `deleteEsame - chiama checkObiettivi dopo eliminazione`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.deleteEsame(esame1)
        advanceUntilIdle()

        verify(deleteEsame)(esame1)
        verify(checkObiettivi)()
    }

    @Test
    fun `updateEsame - non chiama checkObiettivi`() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.updateEsame(esame1)
        advanceUntilIdle()

        verify(updateEsame)(esame1)
        // verify(checkObiettivi, never())() — il check non è necessario per update
    }
}
