package com.unibo.android.corsolp2526

import android.app.Application
import com.unibo.android.data.di.RepositoryProviderImpl
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.di.UseCasesProvider
import com.unibo.android.domain.repository.AuthRepository
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository

class CustomApplication : Application(), RepositoryProvider {

    private lateinit var repositoryProviderImpl: RepositoryProviderImpl

    override fun onCreate() {
        super.onCreate()
        repositoryProviderImpl = RepositoryProviderImpl(this)
        UseCasesProvider.setup(repositoryProviderImpl)
    }

    override fun getEsameRepository(): EsameRepository =
        repositoryProviderImpl.getEsameRepository()

    override fun getObiettivoRepository(): ObiettivoRepository =
        repositoryProviderImpl.getObiettivoRepository()

    override fun getAuthRepository(): AuthRepository =
        repositoryProviderImpl.getAuthRepository()
}
