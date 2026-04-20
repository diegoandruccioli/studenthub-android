package com.unibo.android.domain.di

import com.unibo.android.domain.usecases.FetchAccommodationListUpdatesUseCase
import com.unibo.android.domain.usecases.FetchAccommodationListUpdatesUseCaseImpl
import com.unibo.android.domain.usecases.StartFetchAccommodationListUseCase
import com.unibo.android.domain.usecases.StartFetchAccommodationListUseCaseImpl

object UseCasesProvider {

    lateinit var startFetchAccommodationListUseCase: StartFetchAccommodationListUseCase
    lateinit var fetchAccommodationListUpdatesUseCase: FetchAccommodationListUpdatesUseCase

    fun setup(
        repositoryProvider: RepositoryProvider
    ) {
        startFetchAccommodationListUseCase = StartFetchAccommodationListUseCaseImpl(
            accommodationRepository = repositoryProvider.accommodationRepository
        )

        fetchAccommodationListUpdatesUseCase = FetchAccommodationListUpdatesUseCaseImpl(
            accommodationRepository = repositoryProvider.accommodationRepository
        )
    }
}