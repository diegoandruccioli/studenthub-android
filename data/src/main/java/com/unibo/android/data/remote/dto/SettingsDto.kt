package com.unibo.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SettingsResponse(
    @SerializedName("tema_voti") val temaVoti: String,
    @SerializedName("rgb_soglia_bassa") val rgbSogliaBassa: Int,
    @SerializedName("rgb_soglia_alta") val rgbSogliaAlta: Int
)

data class SettingsRequest(
    @SerializedName("tema_voti") val temaVoti: String,
    @SerializedName("rgb_soglia_bassa") val rgbSogliaBassa: Int,
    @SerializedName("rgb_soglia_alta") val rgbSogliaAlta: Int
)
