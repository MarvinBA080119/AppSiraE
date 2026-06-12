package com.example.appsira.core.repositories

import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Reserva

interface ReservasService {
    suspend fun crearReserva(reserva: Reserva): ResponseService<Unit>
    suspend fun actualizarReserva(reservaIdActual: String, reserva: Reserva): ResponseService<Unit>
    suspend fun cancelarReserva(reservaId: String): ResponseService<Unit>
    suspend fun getMisReservas(): ResponseService<List<Reserva>>
    suspend fun verificarDisponibilidad(auditorioId: Int, fecha: String): ResponseService<Boolean>
}
