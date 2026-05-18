package com.unibo.android.data.remote

import com.unibo.android.data.remote.dto.AddExamResponse
import com.unibo.android.data.remote.dto.ExamDto
import com.unibo.android.data.remote.dto.ExamRequest
import com.unibo.android.data.remote.dto.StatsResponseDto
import com.unibo.android.data.remote.dto.UpdateExamResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ExamApiService {
    @GET("exams")
    suspend fun getEsami(): Response<List<ExamDto>>

    @POST("exams")
    suspend fun addEsami(@Body esami: List<ExamRequest>): Response<AddExamResponse>

    @PUT("exams/{id}")
    suspend fun updateEsame(@Path("id") id: Int, @Body body: ExamRequest): Response<UpdateExamResponse>

    @DELETE("exams/{id}")
    suspend fun deleteEsame(@Path("id") id: Int): Response<Unit>

    @GET("stats")
    suspend fun getStatistiche(): Response<StatsResponseDto>
}
