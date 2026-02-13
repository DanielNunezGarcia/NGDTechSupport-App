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
                val adminData = adminSnapshot.data
                val adminUser = adminSnapshot.toObject(UserModel::class.java)
                
                // Normalizar rol y mapear company a companyId si es necesario
                adminUser?.copy(
                    role = normalizeRole(adminData?.get("role")?.toString() ?: adminUser.role),
                    companyId = adminData?.get("companyId")?.toString() 
                        ?: adminData?.get("company")?.toString() 
                        ?: adminUser.companyId.ifEmpty { adminUser.company }
                )
            } else {
                // 2. Si no es admin, miramos en "users"
                val userSnapshot = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (userSnapshot.exists()) {
                    val userData = userSnapshot.data
                    val user = userSnapshot.toObject(UserModel::class.java)
                    
                    // Normalizar rol y mapear company a companyId si es necesario
                    user?.copy(
                        role = normalizeRole(userData?.get("role")?.toString() ?: user.role),
                        companyId = userData?.get("companyId")?.toString() 
                            ?: userData?.get("company")?.toString() 
                            ?: user.companyId.ifEmpty { user.company }
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            // Log del error para debugging (en producción usar Log o un sistema de logging)
            e.printStackTrace()
            // Si es un error de permisos, lo relanzamos para que el ViewModel lo maneje
            if (e.message?.contains("permission") == true || 
                e.message?.contains("PERMISSION_DENIED") == true) {
                throw Exception("Error de permisos: Verifica las reglas de seguridad de Firestore. UID: $uid", e)
            }
            null
        }
    }

    /**
     * Normaliza el rol a mayúsculas para consistencia
     * "Client" -> "CLIENT", "Admin" -> "ADMIN", etc.
     */
    private fun normalizeRole(role: String): String {
        return when {
            role.isBlank() -> "CLIENT"
            role.equals("admin", ignoreCase = true) -> "ADMIN"
            role.equals("client", ignoreCase = true) -> "CLIENT"
            role.equals("soporte", ignoreCase = true) -> "SOPORTE"
            else -> role.uppercase()
        }
    }
}