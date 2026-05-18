package com.unibo.android.corsolp2526

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.unibo.android.data.di.RepositoryProviderImpl
import com.unibo.android.data.local.SessionDataStore
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.worker.SyncExamsWorker
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.di.UseCasesProvider
import com.unibo.android.domain.repository.AuthRepository
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import com.unibo.android.domain.repository.SettingsRepository
import java.util.concurrent.TimeUnit

class CustomApplication : Application(), RepositoryProvider {

    private lateinit var repositoryProviderImpl: RepositoryProviderImpl

    override fun onCreate() {
        super.onCreate()
        NetworkClient.init(SessionDataStore(this))
        repositoryProviderImpl = RepositoryProviderImpl(this)
        UseCasesProvider.setup(repositoryProviderImpl)
        scheduleSyncWorker()
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncExamsWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncExamsWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun getEsameRepository(): EsameRepository =
        repositoryProviderImpl.getEsameRepository()

    override fun getObiettivoRepository(): ObiettivoRepository =
        repositoryProviderImpl.getObiettivoRepository()

    override fun getAuthRepository(): AuthRepository =
        repositoryProviderImpl.getAuthRepository()

    override fun getSettingsRepository(): SettingsRepository =
        repositoryProviderImpl.getSettingsRepository()
}
