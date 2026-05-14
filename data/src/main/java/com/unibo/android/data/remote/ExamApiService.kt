package com.unibo.android.data.remote

import com.unibo.android.data.remote.dto.AddExamResponse
import com.unibo.android.data.remote.dto.ExamDto
import com.unibo.android.data.remote.dto.ExamRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExamApiService {
    @GET("exams")
    suspend fun getEsami(): Response<List<ExamDto>>

    @POST("exams")
    suspend fun addEsami(@Body esami: List<ExamRequest>): Response<AddExamResponse>

    @DELETE("exams/{id}")
    suspend fun deleteEsame(@Path("id") id: Int): Response<Unit>
}
