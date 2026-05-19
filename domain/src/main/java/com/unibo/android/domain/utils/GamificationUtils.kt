package com.unibo.android.domain.utils

object GamificationUtils {
    const val XP_PER_LEVEL = 500

    /**
     * Calcola il livello corrente in base agli XP (1 livello ogni 500 XP).
     * Livello 1: 0 - 499 XP
     * Livello 2: 500 - 999 XP, ecc.
     */
    fun calculateLevel(xp: Int): Int {
        if (xp < 0) return 1
        return (xp / XP_PER_LEVEL) + 1
    }

    /**
     * Calcola gli XP correnti all'interno del livello attuale.
     * Es: 600 XP totali -> 100 XP (su 500) del livello 2.
     */
    fun getXpInCurrentLevel(xp: Int): Int {
        if (xp < 0) return 0
        return xp % XP_PER_LEVEL
    }

    /**
     * Restituisce una stringa con il titolo in base al livello.
     * (Esempio basato sui mockup).
     */
    fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Matricola Dispersa"
            2 -> "Studente Novizio"
            3 -> "Studente Impegnato"
            4 -> "Laureando Speranzoso"
            5 -> "Dottore in Erba"
            else -> "Maestro Universitario"
        }
    }
}
