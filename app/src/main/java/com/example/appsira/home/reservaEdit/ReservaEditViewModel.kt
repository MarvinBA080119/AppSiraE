package com.example.appsira.home.reservaEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Reserva
import com.example.appsira.core.repositories.ReservasRepository
import com.example.appsira.core.repositories.ReservasService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservaEditViewModel(
    private val service: ReservasService = ReservasRepository()
) : ViewModel() {

    private lateinit var reservaOriginal: Reserva
    private var initialized = false

    private val _fechaSeleccionada = MutableStateFlow<String?>(null)
    val fechaSeleccionada: StateFlow<String?> = _fechaSeleccionada.asStateFlow()

    private val _horaSeleccionada = MutableStateFlow<String?>(null)
    val horaSeleccionada: StateFlow<String?> = _horaSeleccionada.asStateFlow()

    private val _disponibilidadState = MutableStateFlow<ResponseService<Boolean>?>(null)
    val disponibilidadState: StateFlow<ResponseService<Boolean>?> = _disponibilidadState.asStateFlow()

    private val _guardarState = MutableStateFlow<ResponseService<Unit>?>(null)
    val guardarState: StateFlow<ResponseService<Unit>?> = _guardarState.asStateFlow()

    private val _eliminarState = MutableStateFlow<ResponseService<Unit>?>(null)
    val eliminarState: StateFlow<ResponseService<Unit>?> = _eliminarState.asStateFlow()

    /** Carga la reserva a editar (solo la primera vez, sobrevive rotaciones). */
    fun init(reserva: Reserva) {
        if (initialized) return
        initialized = true
        reservaOriginal = reserva
        _fechaSeleccionada.value = reserva.fecha
        _horaSeleccionada.value = reserva.hora
        // Su propia fecha siempre está disponible para sí mismo.
        _disponibilidadState.value = ResponseService.Success(true)
    }

    fun seleccionarFecha(fecha: String) {
        _fechaSeleccionada.value = fecha
        val nuevoId = ReservasRepository.buildDocId(reservaOriginal.auditorioId, fecha)
        if (nuevoId == reservaOriginal.id) {
            // Es la misma fecha de SU reserva: disponible para él.
            _disponibilidadState.value = ResponseService.Success(true)
            return
        }
        viewModelScope.launch {
            _disponibilidadState.value = ResponseService.Loading
            _disponibilidadState.value =
                service.verificarDisponibilidad(reservaOriginal.auditorioId, fecha)
        }
    }

    fun seleccionarHora(hora: String) {
        _horaSeleccionada.value = hora
    }

    fun guardarCambios(
        nombreEvento: String,
        sistemaAudio: Boolean,
        maestroCeremonia: Boolean,
        mesaCentral: Boolean,
        numMicrofonos: Int
    ) {
        if (nombreEvento.isBlank()) {
            _guardarState.value = ResponseService.Error("Escribe el nombre del evento")
            return
        }
        val fecha = _fechaSeleccionada.value
        val hora = _horaSeleccionada.value
        if (fecha == null) {
            _guardarState.value = ResponseService.Error("Selecciona una fecha para tu reserva")
            return
        }
        if (hora == null) {
            _guardarState.value = ResponseService.Error("Selecciona una hora para tu reserva")
            return
        }

        val actualizada = reservaOriginal.copy(
            nombreEvento = nombreEvento.trim(),
            fecha = fecha,
            hora = hora,
            sistemaAudio = sistemaAudio,
            maestroCeremonia = maestroCeremonia,
            mesaCentral = mesaCentral,
            numMicrofonos = numMicrofonos
        )

        viewModelScope.launch {
            _guardarState.value = ResponseService.Loading
            _guardarState.value = service.actualizarReserva(reservaOriginal.id, actualizada)
        }
    }

    fun eliminarReserva() {
        viewModelScope.launch {
            _eliminarState.value = ResponseService.Loading
            _eliminarState.value = service.cancelarReserva(reservaOriginal.id)
        }
    }

    fun consumeGuardarState() {
        _guardarState.value = null
    }

    fun consumeEliminarState() {
        _eliminarState.value = null
    }
}
