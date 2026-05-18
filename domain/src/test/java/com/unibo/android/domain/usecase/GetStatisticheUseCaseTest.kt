package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class GetStatisticheUseCaseTest {

    private val repo: EsameRepository = mock()
    private val useCase = GetStatisticheUseCase(repo)

    private fun esame(voto: Int, cfu: Int, data: LocalDate) =
        Esame(nome = "Materia", voto = voto, lode = false, cfu = cfu, dataEsame = data)

    @Test
    fun `lista vuota restituisce statistiche a zero`() = runTest {
        whenever(repo.getEsami()).thenReturn(flowOf(emptyList()))

        val result = useCase().first()
        val stats = result.getOrThrow()

        assertEquals(0.0, stats.mediaPonderata, 0.001)
        assertEquals(0, stats.cfuSostenuti)
        assertEquals(0.0, stats.baseLaurea, 0.001)
        assertTrue(stats.andamentoCarriera.isEmpty())
    }

    @Test
    fun `un esame solo - media ponderata uguale al voto`() = runTest {
        whenever(repo.getEsami()).thenReturn(
            flowOf(listOf(esame(voto = 27, cfu = 6, data = LocalDate.of(2025, 1, 10))))
        )

        val stats = useCase().first().getOrThrow()

        assertEquals(27.0, stats.mediaPonderata, 0.001)
        assertEquals(6, stats.cfuSostenuti)
        assertEquals(1, stats.andamentoCarriera.size)
    }

    @Test
    fun `media ponderata calcolata correttamente su più esami`() = runTest {
        // (28*6 + 30*12) / (6+12) = (168 + 360) / 18 = 528/18 = 29.33...
        whenever(repo.getEsami()).thenReturn(
            flowOf(listOf(
                esame(voto = 28, cfu = 6, data = LocalDate.of(2025, 1, 10)),
                esame(voto = 30, cfu = 12, data = LocalDate.of(2025, 2, 20))
            ))
        )

        val stats = useCase().first().getOrThrow()

        assertEquals(29.333, stats.mediaPonderata, 0.001)
        assertEquals(18, stats.cfuSostenuti)
    }

    @Test
    fun `base laurea uguale a mediaPonderata per 110 diviso 30`() = runTest {
        whenever(repo.getEsami()).thenReturn(
            flowOf(listOf(esame(voto = 27, cfu = 6, data = LocalDate.of(2025, 1, 10))))
        )

        val stats = useCase().first().getOrThrow()
        val attesa = (27.0 * 110.0) / 30.0

        assertEquals(attesa, stats.baseLaurea, 0.001)
    }

    @Test
    fun `andamento carriera ordinato per data crescente`() = runTest {
        val data1 = LocalDate.of(2025, 3, 1)
        val data2 = LocalDate.of(2025, 1, 1)
        whenever(repo.getEsami()).thenReturn(
            flowOf(listOf(
                esame(voto = 25, cfu = 6, data = data1),
                esame(voto = 30, cfu = 6, data = data2)
            ))
        )

        val stats = useCase().first().getOrThrow()

        assertEquals(data2, stats.andamentoCarriera[0].data)
        assertEquals(data1, stats.andamentoCarriera[1].data)
    }

    @Test
    fun `media ponderata progressiva nell andamento cresce correttamente`() = runTest {
        // Esame 1: 24 voto, 6 CFU → media = 24.0
        // Esame 2: 30 voto, 6 CFU → media = (24*6 + 30*6) / 12 = 27.0
        whenever(repo.getEsami()).thenReturn(
            flowOf(listOf(
                esame(voto = 24, cfu = 6, data = LocalDate.of(2025, 1, 1)),
                esame(voto = 30, cfu = 6, data = LocalDate.of(2025, 2, 1))
            ))
        )

        val andamento = useCase().first().getOrThrow().andamentoCarriera

        assertEquals(24.0, andamento[0].mediaPonderataProgressiva, 0.001)
        assertEquals(27.0, andamento[1].mediaPonderataProgressiva, 0.001)
    }
}
