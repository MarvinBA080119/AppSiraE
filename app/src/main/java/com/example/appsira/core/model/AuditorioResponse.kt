package com.example.appsira.core.model

import com.google.gson.annotations.SerializedName

data class AuditorioResponse(
    @SerializedName(value = "auditorios") val auditorios: List<Auditorio>
)

data class Auditorio(
    @SerializedName(value = "id") val id: Int,
    @SerializedName(value = "nombre") val nombre: String,
    @SerializedName(value = "capacidad") val capacidad: Int,
    @SerializedName(value = "imagen") val imagen: String
)
