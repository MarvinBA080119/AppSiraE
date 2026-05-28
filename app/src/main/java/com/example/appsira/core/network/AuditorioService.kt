package com.example.appsira.core.network

import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio

interface AuditorioService {
    suspend fun getTracks(limit: Int = 20): ResponseService<List<Auditorio>>
}