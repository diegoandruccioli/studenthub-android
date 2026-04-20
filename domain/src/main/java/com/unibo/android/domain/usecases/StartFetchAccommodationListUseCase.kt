package com.unibo.android.domain.usecases

import com.unibo.android.domain.repositories.AccommodationRepository

interface StartFetchAccommodationListUseCase {
    fun invoke()
}

class StartFetchAccommodationListUseCaseImpl(
    val accommodationRepository: AccommodationRepository
): StartFetchAccommodationListUseCase {
    override fun invoke() {
        accommodationRepository.startFetchAccommodationList()
    }
}