package com.example.appsira.core.repositories

import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio
import com.example.appsira.core.network.ApiClient
import com.google.api.QuotaLimit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuditorioRepository {
    private val api = ApiClient.AuditorioApi

    override suspend fun getTraks(limit: Int=20): ResponseService<Auditorio> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getTraks()
                val body = response.auditorios
                if (body.isNotEmpty()) {
                    ResponseService.Success(data = body)
                } else {
                    ResponseService.Error("Respuesta vacía del servidor")
                }
            } catch (e: Exception) {
                ResponseService.Error(
                    "No se pudieron cargar los auditorios: ${e.localizedMessage}"
                )
            }
        }