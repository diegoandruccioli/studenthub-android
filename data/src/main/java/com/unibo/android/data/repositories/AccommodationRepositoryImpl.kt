package com.unibo.android.data.repositories

import com.unibo.android.domain.models.AccommodationType
import com.unibo.android.domain.repositories.AccommodationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AccommodationRepositoryImpl: AccommodationRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun getMockUiAccomodationTypes() = listOf(
        AccommodationType.Hotel(
            hotelName = "Hotel Rosalba",
            hotelDescription = "Questa è la descrizione dell'hotel Rosalba che si trova a Cesena, in via Marina 4",
            hotelPictureUrl = "https://picsum.photos/200/300",
            hotelScore = 8.4
        ),
        AccommodationType.Hotel(
            hotelName = "Hotel Internazionale",
            hotelDescription = "Questa è la descrizione dell'hotel Internazionale che si trova a Cesena, in via Marina 4",
            hotelPictureUrl = "https://picsum.photos/200/300",
            hotelScore = 7.5
        ),

        AccommodationType.Apartment(
            apartmentName = "Appartamenti Sul Mare",
            apartmentDescription = "Questa è la descrizione degli appartamenti sul Mare che si trova a Cesena, in via Marina 4",
            apartmentPictureUrl = "https://picsum.photos/200/300",
            apartmentScore = 9.8
        ),

        AccommodationType.Hotel(
            hotelName = "Hotel Cesare",
            hotelDescription = "Questa è la descrizione dell'hotel Cesare che si trova a Cesena, in via Marina 4",
            hotelPictureUrl = "https://picsum.photos/200/300",
            hotelScore = 3.8
        ),

        AccommodationType.Apartment(
            apartmentName = "Appartamenti Nuovi",
            apartmentDescription = "Questa è la descrizione degli appartamenti Nuovi che si trova a Cesena, in via Marina 4",
            apartmentPictureUrl = "https://picsum.photos/200/300",
            apartmentScore = 7.2
        ),

        AccommodationType.Hotel(
            hotelName = "Hotel Majestic",
            hotelDescription = "Questa è la descrizione dell'hotel Majestic che si trova a Cesena, in via Marina 4",
            hotelPictureUrl = "https://picsum.photos/200/300",
            hotelScore = 8.0
        ),

        AccommodationType.Apartment(
            apartmentName = "Appartamenti Cesira",
            apartmentDescription = "Questa è la descrizione degli appartamenti Cesira che si trova a Cesena, in via Marina 4",
            apartmentPictureUrl = "https://picsum.photos/200/300",
            apartmentScore = 6.7
        ),
    )

    private val _accommodationTypeList = MutableStateFlow<List<AccommodationType>>(emptyList())
    override val accommodationTypeList: StateFlow<List<AccommodationType>> = _accommodationTypeList

    override fun startFetchAccommodationList() {
        scope.launch {
            delay(5.seconds)
            _accommodationTypeList.emit(
                getMockUiAccomodationTypes()
            )
        }
    }
}