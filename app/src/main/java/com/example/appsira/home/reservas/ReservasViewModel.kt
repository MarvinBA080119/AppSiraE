package com.example.appsira.home.reservas

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

class ReservasViewModel(
    private val service: ReservasService = ReservasRepository()
) : ViewModel() {

    private val _reservasState = MutableStateFlow<ResponseService<List<Reserva>>?>(null)
    val reservasState: StateFlow<ResponseService<List<Reserva>>?> = _reservasState.asStateFlow()

    private val _cancelarState = MutableStateFlow<ResponseService<Unit>?>(null)
    val cancelarState: StateFlow<ResponseService<Unit>?> = _cancelarState.asStateFlow()

    fun loadReservas() {
        viewModelScope.launch {
            _reservasState.value = ResponseService.Loading
            _reservasState.value = service.getMisReservas()
        }
    }

    fun cancelarReserva(reservaId: String) {
        viewModelScope.launch {
            _cancelarState.value = ResponseService.Loading
            val result = service.cancelarReserva(reservaId)
            _cancelarState.value = result
            if (result is ResponseService.Success) {
                loadReservas()
            }
        }
    }

    fun consumeCancelarState() {
        _cancelarState.value = null
    }
}
