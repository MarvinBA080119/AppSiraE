package com.example.appsira.core.network

import com.example.appsira.core.model.AuditorioResponse
import retrofit2.http.GET
import retrofit2.Response

interface AuditorioAPI {
    @GET ("api.mockbin.io")
    suspend fun getTraks(limit: Int): Response<AuditorioResponse>
}