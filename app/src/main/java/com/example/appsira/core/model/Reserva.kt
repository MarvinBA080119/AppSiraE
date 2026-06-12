package com.example.appsira.core.model

data class Reserva(
    val id: String = "",
    val auditorioId: Int = 0,
    val auditorioNombre: String = "",
    val auditorioImagen: String = "",
    val auditorioCapacidad: Int = 0,
    val userId: String = "",
    val userEmail: String = "",
    val nombreEvento: String = "",
    val fecha: String = "",
    val hora: String = "",
    val sistemaAudio: Boolean = false,
    val maestroCeremonia: Boolean = false,
    val mesaCentral: Boolean = false,
    val numMicrofonos: Int = 0,
    val createdAt: Long = 0L
)
