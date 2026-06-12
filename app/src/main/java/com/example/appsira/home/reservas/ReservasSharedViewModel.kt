package com.example.appsira.home.reservas

import androidx.lifecycle.ViewModel
import com.example.appsira.core.model.Reserva
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel compartido a nivel de Activity para pasar la reserva
 * seleccionada a la pantalla de edición (mismo patrón que
 * AuditoriosSharedViewModel).
 */
class ReservasSharedViewModel : ViewModel() {
    private val _selectedReserva = MutableStateFlow<Reserva?>(null)
    val selectedReserva: StateFlow<Reserva?> = _selectedReserva.asStateFlow()

    fun selectReserva(reserva: Reserva) {
        _selectedReserva.value = reserva
    }
}
