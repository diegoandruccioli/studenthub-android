package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalEvaluatorTest {

    private fun esame(
        voto: Int = 27,
        cfu: Int = 6,
        lode: Boolean = false,
        data: LocalDate = LocalDate.of(2025, 1, 15)
    ) = Esame(nome = "Test", voto = voto, lode = lode, cfu = cfu, dataEsame = data)

    // ── PrimoPassoEvaluator ──────────────────────────────────────────────────

    @Test
    fun `PrimoPassoEvaluator - lista vuota restituisce false`() {
        assertFalse(PrimoPassoEvaluator().evaluate(emptyList()))
    }

    @Test
    fun `PrimoPassoEvaluator - almeno un esame restituisce true`() {
        assertTrue(PrimoPassoEvaluator().evaluate(listOf(esame())))
    }

    // ── SecchioneEvaluator ───────────────────────────────────────────────────

    @Test
    fun `SecchioneEvaluator - nessuna lode restituisce false`() {
        val esami = listOf(esame(voto = 28), esame(voto = 30))
        assertFalse(SecchioneEvaluator().evaluate(esami))
    }

    @Test
    fun `SecchioneEvaluator - almeno una lode restituisce true`() {
        val esami = listOf(esame(voto = 30, lode = true), esame(voto = 25))
        assertTrue(SecchioneEvaluator().evaluate(esami))
    }

    // ── MaratonetaEvaluator ──────────────────────────────────────────────────

    @Test
    fun `MaratonetaEvaluator - meno di 3 esami in un mese restituisce false`() {
        val esami = listOf(
            esame(data = LocalDate.of(2025, 1, 10)),
            esame(data = LocalDate.of(2025, 1, 20)),
            esame(data = LocalDate.of(2025, 2, 5))
        )
        assertFalse(MaratonetaEvaluator().evaluate(esami))
    }

    @Test
    fun `MaratonetaEvaluator - 3 esami nello stesso mese restituisce true`() {
        val esami = listOf(
            esame(data = LocalDate.of(2025, 1, 5)),
            esame(data = LocalDate.of(2025, 1, 15)),
            esame(data = LocalDate.of(2025, 1, 25))
        )
        assertTrue(MaratonetaEvaluator().evaluate(esami))
    }

    // ── GiroDiBoaEvaluator ───────────────────────────────────────────────────

    @Test
    fun `GiroDiBoaEvaluator - CFU totali sotto 90 restituisce false`() {
        val esami = listOf(esame(cfu = 12), esame(cfu = 30), esame(cfu = 47))
        assertFalse(GiroDiBoaEvaluator().evaluate(esami))
    }

    @Test
    fun `GiroDiBoaEvaluator - CFU totali uguale a 90 restituisce true`() {
        val esami = listOf(esame(cfu = 30), esame(cfu = 30), esame(cfu = 30))
        assertTrue(GiroDiBoaEvaluator().evaluate(esami))
    }

    @Test
    fun `GiroDiBoaEvaluator - CFU totali sopra 90 restituisce true`() {
        val esami = listOf(esame(cfu = 48), esame(cfu = 48))
        assertTrue(GiroDiBoaEvaluator().evaluate(esami))
    }
}
