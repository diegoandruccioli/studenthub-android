package com.unibo.android.domain.utils

object GamificationUtils {
    const val XP_PER_LEVEL = 500

    fun calculateLevel(xp: Int): Int {
        if (xp < 0) return 1
        return (xp / XP_PER_LEVEL) + 1
    }

    fun getXpInCurrentLevel(xp: Int): Int {
        if (xp < 0) return 0
        return xp % XP_PER_LEVEL
    }

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
