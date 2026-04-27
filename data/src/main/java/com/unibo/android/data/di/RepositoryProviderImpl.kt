package com.unibo.android.data.di

import android.content.Context
import com.unibo.android.data.repository.EsameRepositoryImpl
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.repository.EsameRepository

class RepositoryProviderImpl(private val context: Context) : RepositoryProvider {
    private val esameRepository: EsameRepository by lazy { EsameRepositoryImpl(context) }
    override fun getEsameRepository(): EsameRepository = esameRepository
}
