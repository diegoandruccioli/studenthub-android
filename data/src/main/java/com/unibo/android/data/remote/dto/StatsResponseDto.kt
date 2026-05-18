package com.unibo.android.data.remote.dto

data class StatsResponseDto(
    val mediaAritmetica: Double,
    val mediaPonderata: Double,
    val baseLaurea: Double,
    val totaleCfu: Int,
    val chartData: ChartDataDto
)

data class ChartDataDto(
    val labels: List<String>,
    val data: List<Int>,
    val examNames: List<String>
)
