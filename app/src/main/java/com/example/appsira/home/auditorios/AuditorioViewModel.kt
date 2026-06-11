package com.example.appsira.home.auditorios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio
import com.example.appsira.core.network.AuditorioService
import com.example.appsira.core.repositories.AuditorioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuditorioViewModel(
    private val service: AuditorioService = AuditorioRepository()
) : ViewModel() {

    private val _auditorioState = MutableStateFlow<ResponseService<List<Auditorio>>?>(null)
    val auditorioState: StateFlow<ResponseService<List<Auditorio>>?> = _auditorioState.asStateFlow()

    fun loadAuditorios() {
        viewModelScope.launch {
            _auditorioState.value = ResponseService.Loading
            _auditorioState.value = service.getAuditorios()
        }
    }
}
