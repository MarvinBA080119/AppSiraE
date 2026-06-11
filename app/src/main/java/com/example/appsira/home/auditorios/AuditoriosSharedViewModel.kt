package com.example.appsira.home.auditorios

import androidx.lifecycle.ViewModel
import com.example.appsira.core.model.Auditorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuditoriosSharedViewModel : ViewModel() {
    private val _selectedAuditorio = MutableStateFlow<Auditorio?>(null)
    val selectedAuditorio: StateFlow<Auditorio?> = _selectedAuditorio.asStateFlow()

    fun selectAuditorio(auditorio: Auditorio) {
        _selectedAuditorio.value = auditorio
    }
}
