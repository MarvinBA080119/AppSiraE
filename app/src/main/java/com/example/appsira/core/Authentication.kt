package com.example.appsira.core

import com.google.firebase.auth.FirebaseUser

interface Authentication {
    suspend fun requestLogin(email: String, password: String): ResponseService<FirebaseUser>
    suspend fun requestSignUp(email: String, password: String): ResponseService<FirebaseUser>
    suspend fun requestPasswordReset(email: String): ResponseService<Unit>
    suspend fun saveUserInfo(
        uid: String,
        name: String,
        lastName: String,
        username: String,
        phone: String,
        birthDate: String
    ): ResponseService<Unit>
}
