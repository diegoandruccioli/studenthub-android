package com.unibo.android.domain.repositories

import com.unibo.android.domain.models.AccommodationType
import kotlinx.coroutines.flow.StateFlow

interface AccommodationRepository {

    val accommodationTypeList: StateFlow<List<AccommodationType>>

    fun startFetchAccommodationList()
}