package com.unibo.android.domain.usecases

import com.unibo.android.domain.models.AccommodationType
import com.unibo.android.domain.repositories.AccommodationRepository
import kotlinx.coroutines.flow.StateFlow

interface FetchAccommodationListUpdatesUseCase {
    fun invoke(): StateFlow<List<AccommodationType>>
}

class FetchAccommodationListUpdatesUseCaseImpl(
    val accommodationRepository: AccommodationRepository
): FetchAccommodationListUpdatesUseCase {
    override fun invoke(): StateFlow<List<AccommodationType>> {
        return accommodationRepository.accommodationTypeList
    }
}