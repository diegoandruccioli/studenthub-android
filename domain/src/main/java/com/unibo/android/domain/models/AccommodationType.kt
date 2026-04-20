package com.unibo.android.domain.models

sealed class AccommodationType(
    val name: String,
    val description: String,
    val pictureUrl: String,
    val score: Double
) {
    data class Hotel(
        private val hotelName: String,
        private val hotelDescription: String,
        private val hotelPictureUrl: String,
        private val hotelScore: Double,
        //val hotelRanking: Int
    ): AccommodationType(
        name = hotelName,
        description = hotelDescription,
        pictureUrl = hotelPictureUrl,
        score = hotelScore
    )

    data class Apartment(
        private val apartmentName: String,
        private val apartmentDescription: String,
        private val apartmentPictureUrl: String,
        private val apartmentScore: Double
    ): AccommodationType(
        name = apartmentName,
        description = apartmentDescription,
        pictureUrl = apartmentPictureUrl,
        score = apartmentScore
    )
}