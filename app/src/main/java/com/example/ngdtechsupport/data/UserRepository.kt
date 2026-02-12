package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    // Devuelve el usuario y su rol, comprobando primero la colección "admin" y luego "users"
    suspend fun getUser(uid: String): UserModel? {
        return try {
            // 1. Primero miramos si es admin
            val adminSnapshot = db.collection("admin")
                .document(uid)
                .get()
                .await()

            if (adminSnapshot.exists()) {
                // Si quieres, puedes forzar el rol a "admin" aquí
                val adminUser = adminSnapshot.toObject(UserModel::class.java)
                adminUser?.copy(role = if (adminUser.role.isBlank()) "admin" else adminUser.role)
            } else {
                // 2. Si no es admin, miramos en "users"
                val userSnapshot = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (userSnapshot.exists()) {
                    val user = userSnapshot.toObject(UserModel::class.java)
                    user?.copy(role = if (user.role.isBlank()) "user" else user.role)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}