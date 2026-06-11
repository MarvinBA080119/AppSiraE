package com.example.appsira.core.network

import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio

interface AuditorioService {
    suspend fun getAuditorios(): ResponseService<List<Auditorio>>
}