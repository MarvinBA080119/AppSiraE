package com.example.appsira.home.auditorioDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio
import com.example.appsira.core.model.Reserva
import com.example.appsira.core.repositories.ReservasRepository
import com.example.appsira.core.repositories.ReservasService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuditorioDetailViewModel(
    private val reservasService: ReservasService = ReservasRepository()
) : ViewModel() {

    private val _fechaSeleccionada = MutableStateFlow<String?>(null)
    val fechaSeleccionada: StateFlow<String?> = _fechaSeleccionada.asStateFlow()

    private val _horaSeleccionada = MutableStateFlow<String?>(null)
    val horaSeleccionada: StateFlow<String?> = _horaSeleccionada.asStateFlow()

    private val _disponibilidadState = MutableStateFlow<ResponseService<Boolean>?>(null)
    val disponibilidadState: StateFlow<ResponseService<Boolean>?> = _disponibilidadState.asStateFlow()

    private val _reservaState = MutableStateFlow<ResponseService<Unit>?>(null)
    val reservaState: StateFlow<ResponseService<Unit>?> = _reservaState.asStateFlow()

    fun seleccionarFecha(fecha: String, auditorioId: Int) {
        _fechaSeleccionada.value = fecha
        verificarDisponibilidad(auditorioId, fecha)
    }

    fun seleccionarHora(hora: String) {
        _horaSeleccionada.value = hora
    }

    private fun verificarDisponibilidad(auditorioId: Int, fecha: String) {
        viewModelScope.launch {
            _disponibilidadState.value = ResponseService.Loading
            _disponibilidadState.value = reservasService.verificarDisponibilidad(auditorioId, fecha)
        }
    }

    fun reservar(
        auditorio: Auditorio,
        sistemaAudio: Boolean,
        maestroCeremonia: Boolean,
        mesaCentral: Boolean,
        numMicrofonos: Int
    ) {
        val fecha = _fechaSeleccionada.value
        val hora = _horaSeleccionada.value

        if (fecha == null) {
            _reservaState.value = ResponseService.Error("Selecciona una fecha para tu reserva")
            return
        }
        if (hora == null) {
            _reservaState.value = ResponseService.Error("Selecciona una hora para tu reserva")
            return
        }

        val reserva = Reserva(
            auditorioId = auditorio.id,
            auditorioNombre = auditorio.nombre,
            auditorioImagen = auditorio.imagen,
            auditorioCapacidad = auditorio.capacidad,
            fecha = fecha,
            hora = hora,
            sistemaAudio = sistemaAudio,
            maestroCeremonia = maestroCeremonia,
            mesaCentral = mesaCentral,
            numMicrofonos = numMicrofonos
        )

        viewModelScope.launch {
            _reservaState.value = ResponseService.Loading
            _reservaState.value = reservasService.crearReserva(reserva)
        }
    }

    fun consumeReservaState() {
        _reservaState.value = null
    }
}