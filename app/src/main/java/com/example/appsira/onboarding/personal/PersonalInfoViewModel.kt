package com.example.appsira.onboarding.personal

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.ResponseService
import com.example.appsira.core.repositories.UserRepository
import com.example.appsira.onboarding.personal.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PersonalInfoViewModel: ViewModel(){
    private val repository = UserRepository()

    private val _saveState = MutableStateFlow<ResponseService<Unit>?>(null)
    val saveState: StateFlow<ResponseService<Unit>> = _saveState

    fun validateUsername(value: String): String? {
        if (value.isBlank()) return "El usuario es requerido"
        if (value.length < 4) return "Minimo 4 caracteres"
        if (!value.matches(Regex(pattern = "^[a-zA-Z0-9._]+$")))
            return "Solo letras, números, _ y ."

        return null
    }

    fun validatePhone(value: String): String? {
        if (value.isBlank()) return "El teléfono es requerido"
        if (!value.all { it.isDigit() }) return "Solo números"
        if (value.length !in 10..15) return "Entre 10 y 15 dígitos"

        return null
    }

    fun validateBirthDate(value: String): String? {
        if (value.isBlank()) return "Selecciona tu fecha de nacimiento"

        return null
    }

    fun isFormValid(
        firstName: String,
        lastName: String,
        username: String,
        phone: String,
        birthDate: String
    ): Boolean {

        return validateFirstName(value = firstName) == null &&
                validateLastName(value = lastName) == null &&
                validateUsername(value = username) == null &&
                validatePhone(value = phone) == null &&
                validateBirthDate(value = birthDate) == null
    }

    fun saveProfile() {

        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            val user = UserProfile(id = uid, fistName = firstName, lastName = lastName, phone = phone, birthDate = birthDate)
        }
    }

}