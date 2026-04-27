package com.unibo.android.domain.di

import com.unibo.android.domain.repository.EsameRepository

interface RepositoryProvider {
    fun getEsameRepository(): EsameRepository
}
