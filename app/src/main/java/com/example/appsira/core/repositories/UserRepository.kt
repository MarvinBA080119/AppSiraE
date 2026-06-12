package com.example.appsira.core.repositories

import com.example.appsira.core.ResponseService
import com.example.appsira.onboarding.personal.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository : UserService {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userCollection = firestore.collection("users")

    override suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit> = withContext(
        Dispatchers.IO
    ) {
        try {
            userCollection.document(userProfile.id)
                .set(userProfile)
                .await()
            ResponseService.Success(Unit)
        } catch (e: Exception) {
            ResponseService.Error("No se pudo crear el perfil: ${e.localizedMessage}")
        }
    }

    override suspend fun getUserInfo(): ResponseService<UserProfile> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid
            ?: return@withContext ResponseService.Error("Debes iniciar sesión")
        try {
            val doc = userCollection.document(uid).get().await()
            if (!doc.exists()) {
                return@withContext ResponseService.Error("No se encontró la información del perfil")
            }
            val profile = UserProfile(
                id = doc.id,
                firstName = doc.getString("firstName").orEmpty(),
                lastName = doc.getString("lastName").orEmpty(),
                userName = doc.getString("userName").orEmpty(),
                phone = doc.getString("phone").orEmpty(),
                birthDate = doc.getString("birthDate").orEmpty()
            )
            ResponseService.Success(profile)
        } catch (e: Exception) {
            ResponseService.Error("No se pudo cargar tu información: ${e.localizedMessage}")
        }
    }
}
