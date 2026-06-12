package com.example.appsira.home.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsira.core.ResponseService
import com.example.appsira.core.repositories.UserRepository
import com.example.appsira.core.repositories.UserService
import com.example.appsira.onboarding.personal.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val service: UserService = UserRepository()
) : ViewModel() {

    private val _profileState = MutableStateFlow<ResponseService<UserProfile>?>(null)
    val profileState: StateFlow<ResponseService<UserProfile>?> = _profileState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ResponseService.Loading
            _profileState.value = service.getUserInfo()
        }
    }
}
