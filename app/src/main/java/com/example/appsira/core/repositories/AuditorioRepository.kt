package com.example.appsira.core.repositories

import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio
import com.example.appsira.core.network.ApiClient
import com.example.appsira.core.network.AuditorioService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuditorioRepository: AuditorioService {
    private val api = ApiClient.AuditorioApi

    override suspend fun getTracks(limit: Int): ResponseService<List<Auditorio>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getTraks(
                    limit = limit
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResponseService.Success(body.results)
                    } else {
                        ResponseService.Error("Respuesta vacía del servidor")
                    }
                } else {
                    ResponseService.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                ResponseService.Error(
                    "No se pudieron cargar las canciones: ${e.localizedMessage}"
                )
            }
        }
}
