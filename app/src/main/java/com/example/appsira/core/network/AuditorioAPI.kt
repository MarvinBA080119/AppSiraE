package com.example.appsira.core.network

import com.example.appsira.core.model.AuditorioResponse
import retrofit2.Response
import retrofit2.http.GET

interface AuditorioAPI {
    @GET("/")
    suspend fun getAuditorios(): Response<AuditorioResponse>
}
