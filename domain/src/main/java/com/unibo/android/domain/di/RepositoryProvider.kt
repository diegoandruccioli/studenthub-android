package com.unibo.android.domain.di

import com.unibo.android.domain.repositories.AccommodationRepository

interface RepositoryProvider {
    val accommodationRepository: AccommodationRepository
}