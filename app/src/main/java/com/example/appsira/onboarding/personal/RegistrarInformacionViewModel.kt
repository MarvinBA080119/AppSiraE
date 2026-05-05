package com.example.appsira.onboarding.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.AuthRepository
import com.example.appsira.core.ResponseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrarInformacionViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _saveState = MutableStateFlow<ResponseService<Unit>?>(null)
    val saveState: StateFlow<ResponseService<Unit>?> = _saveState.asStateFlow()

    // --- Validación ---
    fun validateName(name: String): String? {
        if (name.isBlank()) return "El nombre es requerido"
        if (name.length < 2) return "Nombre demasiado corto"
        return null
    }

    fun validateLastName(lastName: String): String? {
        if (lastName.isBlank()) return "Los apellidos son requeridos"
        return null
    }

    fun validateUsername(username: String): String? {
        if (username.isBlank()) return "El nombre de usuario es requerido"
        if (username.length < 3) return "Mínimo 3 caracteres"
        return null
    }

    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return "El teléfono es requerido"
        if (phone.length < 10) return "Teléfono inválido"
        return null
    }

    fun validateBirthDate(birthDate: String): String? {
        if (birthDate.isBlank()) return "La fecha es requerida"
        return null
    }

    fun isFormValid(
        name: String, lastName: String, username: String,
        phone: String, birthDate: String
    ): Boolean {
        return validateName(name) == null &&
                validateLastName(lastName) == null &&
                validateUsername(username) == null &&
                validatePhone(phone) == null &&
                validateBirthDate(birthDate) == null
    }

    // --- Operación de guardado ---
    fun saveUserInfo(
        name: String, lastName: String, username: String,
        phone: String, birthDate: String
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _saveState.value = ResponseService.Error("Sesión no válida. Inicia sesión de nuevo")
            return
        }
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            _saveState.value = authRepository.saveUserInfo(
                uid, name, lastName, username, phone, birthDate
            )
        }
    }
}