package com.example.appsira.core.repositories

import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Reserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ReservasRepository : ReservasService {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reservasCollection = firestore.collection("reservas")

    private fun currentUserId(): String? = auth.currentUser?.uid

    private fun docId(auditorioId: Int, fecha: String) = "${auditorioId}_$fecha"

    override suspend fun crearReserva(reserva: Reserva): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            val userId = currentUserId()
                ?: return@withContext ResponseService.Error("Debes iniciar sesión para reservar")
            val userEmail = auth.currentUser?.email.orEmpty()
            try {
                val docRef = reservasCollection.document(docId(reserva.auditorioId, reserva.fecha))

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    if (snapshot.exists()) {
                        throw ReservaOcupadaException()
                    }
                    val data = mapOf(
                        "userId" to userId,
                        "userEmail" to userEmail,
                        "auditorioId" to reserva.auditorioId,
                        "auditorioNombre" to reserva.auditorioNombre,
                        "auditorioImagen" to reserva.auditorioImagen,
                        "auditorioCapacidad" to reserva.auditorioCapacidad,
                        "fecha" to reserva.fecha,
                        "hora" to reserva.hora,
                        "sistemaAudio" to reserva.sistemaAudio,
                        "maestroCeremonia" to reserva.maestroCeremonia,
                        "mesaCentral" to reserva.mesaCentral,
                        "numMicrofonos" to reserva.numMicrofonos,
                        "createdAt" to System.currentTimeMillis()
                    )
                    transaction.set(docRef, data)
                }.await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                if (e is ReservaOcupadaException || e.cause is ReservaOcupadaException) {
                    ResponseService.Error("Este auditorio ya está ocupado ese día. Elige otra fecha.")
                } else {
                    ResponseService.Error("No se pudo crear la reserva: ${e.localizedMessage}")
                }
            }
        }

    override suspend fun cancelarReserva(reservaId: String): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            val userId = currentUserId()
                ?: return@withContext ResponseService.Error("Debes iniciar sesión")
            try {
                val doc = reservasCollection.document(reservaId).get().await()
                if (!doc.exists()) {
                    return@withContext ResponseService.Error("La reserva ya no existe")
                }
                if (doc.getString("userId") != userId) {
                    return@withContext ResponseService.Error("Solo puedes cancelar tus propias reservas")
                }
                reservasCollection.document(reservaId).delete().await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo cancelar la reserva: ${e.localizedMessage}")
            }
        }

    override suspend fun getMisReservas(): ResponseService<List<Reserva>> =
        withContext(Dispatchers.IO) {
            val userId = currentUserId()
                ?: return@withContext ResponseService.Error("Debes iniciar sesión")
            try {
                val snapshot = reservasCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val reservas = snapshot.documents.mapNotNull { doc ->
                    Reserva(
                        id = doc.id,
                        auditorioId = (doc.getLong("auditorioId") ?: 0L).toInt(),
                        auditorioNombre = doc.getString("auditorioNombre").orEmpty(),
                        auditorioImagen = doc.getString("auditorioImagen").orEmpty(),
                        auditorioCapacidad = (doc.getLong("auditorioCapacidad") ?: 0L).toInt(),
                        userId = doc.getString("userId").orEmpty(),
                        userEmail = doc.getString("userEmail").orEmpty(),
                        fecha = doc.getString("fecha").orEmpty(),
                        hora = doc.getString("hora").orEmpty(),
                        sistemaAudio = doc.getBoolean("sistemaAudio") ?: false,
                        maestroCeremonia = doc.getBoolean("maestroCeremonia") ?: false,
                        mesaCentral = doc.getBoolean("mesaCentral") ?: false,
                        numMicrofonos = (doc.getLong("numMicrofonos") ?: 0L).toInt(),
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }.sortedBy { it.fecha }
                ResponseService.Success(reservas)
            } catch (e: Exception) {
                ResponseService.Error("No se pudieron cargar tus reservas: ${e.localizedMessage}")
            }
        }

    override suspend fun verificarDisponibilidad(
        auditorioId: Int,
        fecha: String
    ): ResponseService<Boolean> = withContext(Dispatchers.IO) {
        try {
            val doc = reservasCollection.document(docId(auditorioId, fecha)).get().await()
            ResponseService.Success(!doc.exists())
        } catch (e: Exception) {
            ResponseService.Error("No se pudo verificar la disponibilidad: ${e.localizedMessage}")
        }
    }
}

/** Excepción interna para señalar que la fecha ya está reservada. */
class ReservaOcupadaException : Exception("ocupado")
