package com.unibo.android.data.di

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.repository.AuthRepositoryImpl
import com.unibo.android.data.repository.EsameRepositoryImpl
import com.unibo.android.data.repository.GamificationRepositoryImpl
import com.unibo.android.data.repository.ObiettivoRepositoryImpl
import com.unibo.android.data.repository.SettingsRepositoryImpl
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.repository.AuthRepository
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import com.unibo.android.domain.repository.SettingsRepository

class RepositoryProviderImpl(private val context: Context) : RepositoryProvider {
    private val db: StudentHubDatabase by lazy { StudentHubDatabase.getInstance(context) }

    private val esameRepo: EsameRepository by lazy { EsameRepositoryImpl(context) }
    private val obiettivoRepo: ObiettivoRepository by lazy { ObiettivoRepositoryImpl(db.obiettiviDao()) }
    private val authRepo: AuthRepository by lazy { AuthRepositoryImpl(context) }
    private val settingsRepo: SettingsRepository by lazy { SettingsRepositoryImpl(context) }
    private val gamificationRepo: GamificationRepository by lazy { GamificationRepositoryImpl(context) }

    override fun getEsameRepository(): EsameRepository = esameRepo
    override fun getObiettivoRepository(): ObiettivoRepository = obiettivoRepo
    override fun getAuthRepository(): AuthRepository = authRepo
    override fun getSettingsRepository(): SettingsRepository = settingsRepo
    override fun getGamificationRepository(): GamificationRepository = gamificationRepo
}
