package com.example.appsira.onboarding.password

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.AuthRepository
import com.example.appsira.core.ResponseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecuperarContraViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _recoverState = MutableStateFlow<ResponseService<Unit>?>(null)
    val recoverState: StateFlow<ResponseService<Unit>?> = _recoverState.asStateFlow()

    // --- Validación ---
    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Correo inválido"
        return null
    }

    fun isRecoverFormValid(email: String): Boolean {
        return validateEmail(email) == null
    }

    // --- Operación de recuperación ---
    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _recoverState.value = ResponseService.Loading
            _recoverState.value = authRepository.requestPasswordReset(email)
        }
    }
}