package com.unibo.android.domain.di

import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository

interface RepositoryProvider {
    fun getEsameRepository(): EsameRepository
    fun getObiettivoRepository(): ObiettivoRepository
}
