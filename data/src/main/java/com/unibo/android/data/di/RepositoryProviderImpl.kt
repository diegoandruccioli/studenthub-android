package com.unibo.android.data.di

import android.content.Context
import com.unibo.android.data.repository.AuthRepositoryImpl
import com.unibo.android.data.repository.EsameRepositoryImpl
import com.unibo.android.data.repository.ObiettivoRepositoryImpl
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.repository.AuthRepository
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository

class RepositoryProviderImpl(private val context: Context) : RepositoryProvider {
    private val esameRepo: EsameRepository by lazy { EsameRepositoryImpl(context) }
    private val obiettivoRepo: ObiettivoRepository by lazy { ObiettivoRepositoryImpl(context) }
    private val authRepo: AuthRepository by lazy { AuthRepositoryImpl(context) }

    override fun getEsameRepository(): EsameRepository = esameRepo
    override fun getObiettivoRepository(): ObiettivoRepository = obiettivoRepo
    override fun getAuthRepository(): AuthRepository = authRepo
}
