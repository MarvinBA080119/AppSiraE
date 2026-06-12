package com.example.appsira.core.repositories

import com.example.appsira.core.ResponseService
import com.example.appsira.onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
    suspend fun getUserInfo(): ResponseService<UserProfile>
}