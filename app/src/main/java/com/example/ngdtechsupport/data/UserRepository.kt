package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val defaultCompanyId = "NGDStudios"
    private val defaultBusinessId = "restaurante_madrid"
    private val tag = "UserRepository"

    // Devuelve el usuario y su rol, comprobando primero la colección "admin" y luego "users"
    suspend fun getUser(uid: String): UserModel? {
        Log.d(tag, "getUser called with uid: $uid")
        return try {
            // 1. Primero miramos si es admin
            val adminSnapshot = db.collection("admin")
                .document(uid)
                .get()
                .await()
            Log.d(tag, "Admin snapshot exists: ${adminSnapshot.exists()}")

            if (adminSnapshot.exists()) {
                val adminData = adminSnapshot.data
                Log.d(tag, "Admin data keys: ${adminData?.keys}")
                val adminUser = adminSnapshot.toObject(UserModel::class.java) ?: UserModel()

                // Normalizar rol y mapear company/business con fallback seguro
                val result = adminUser.copy(
                    role = normalizeRole(adminData?.get("role")?.toString() ?: adminUser.role),
                    companyId = resolveCompanyId(
                        primary = adminData?.get("companyId")?.toString(),
                        alternative = adminData?.get("company")?.toString(),
                        modelCompanyId = adminUser.companyId,
                        modelCompany = adminUser.company
                    ),
                    businessId = resolveBusinessId(
                        primary = adminData?.get("businessId")?.toString(),
                        alternative = adminData?.get("business")?.toString(),
                        modelBusinessId = adminUser.businessId
                    )
                )
                Log.d(tag, "Resolved admin user: role=${result.role}, companyId=${result.companyId}, businessId=${result.businessId}")
                result
            } else {
                // 2. Si no es admin, miramos en "users"
                val userSnapshot = db.collection("users")
                    .document(uid)
                    .get()
                    .await()
                Log.d(tag, "User snapshot exists: ${userSnapshot.exists()}")

                if (userSnapshot.exists()) {
                    val userData = userSnapshot.data
                    Log.d(tag, "User data keys: ${userData?.keys}")
                    val user = userSnapshot.toObject(UserModel::class.java) ?: UserModel()

                    // Normalizar rol y mapear company/business con fallback seguro
                    val result = user.copy(
                        role = normalizeRole(userData?.get("role")?.toString() ?: user.role),
                        companyId = resolveCompanyId(
                            primary = userData?.get("companyId")?.toString(),
                            alternative = userData?.get("company")?.toString(),
                            modelCompanyId = user.companyId,
                            modelCompany = user.company
                        ),
                        businessId = resolveBusinessId(
                            primary = userData?.get("businessId")?.toString(),
                            alternative = userData?.get("business")?.toString(),
                            modelBusinessId = user.businessId
                        )
                    )
                    Log.d(tag, "Resolved standard user: role=${result.role}, companyId=${result.companyId}, businessId=${result.businessId}")
                    result
                } else {
                    Log.w(tag, "User not found in users collection")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting user: ${e.message}", e)

            if (e.message?.contains("permission") == true || 
                e.message?.contains("PERMISSION_DENIED") == true) {
                throw Exception("Error de permisos: Verifica las reglas de seguridad de Firestore. UID: $uid", e)
            }

            // No ocultamos errores críticos de red/datos.
            throw Exception("No se pudo obtener el usuario desde Firestore. UID: $uid", e)
        }
    }

    private fun resolveCompanyId(
        primary: String?,
        alternative: String?,
        modelCompanyId: String,
        modelCompany: String
    ): String {
        val candidate = listOf(primary, alternative, modelCompanyId, modelCompany)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()

        val resolved = primary
            .orEmpty()
            .ifBlank { alternative.orEmpty() }
            .ifBlank { modelCompanyId }
            .ifBlank { modelCompany }

        val normalized = normalizeCompanyId(resolved).ifBlank { defaultCompanyId }
        if (normalized != candidate.trim()) {
            Log.w(tag, "Fallback companyId applied. raw='$candidate' resolved='$normalized'")
        }
        return normalized
    }

    private fun resolveBusinessId(
        primary: String?,
        alternative: String?,
        modelBusinessId: String
    ): String {
        val candidate = listOf(primary, alternative, modelBusinessId)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()

        val resolved = primary
            .orEmpty()
            .ifBlank { alternative.orEmpty() }
            .ifBlank { modelBusinessId }

        val normalized = normalizeBusinessId(resolved).ifBlank { defaultBusinessId }
        if (normalized != candidate.trim()) {
            Log.w(tag, "Fallback businessId applied. raw='$candidate' resolved='$normalized'")
        }
        return normalized
    }

    private fun normalizeCompanyId(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return defaultCompanyId
        if (trimmed.equals(defaultCompanyId, ignoreCase = true)) return defaultCompanyId
        return sanitizeFirestoreId(trimmed).ifBlank { defaultCompanyId }
    }

    private fun normalizeBusinessId(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return defaultBusinessId
        if (trimmed.equals(defaultBusinessId, ignoreCase = true)) return defaultBusinessId
        if (trimmed.equals("Restaurante Madrid", ignoreCase = true)) return defaultBusinessId
        if (trimmed.equals("restaurante-madrid", ignoreCase = true)) return defaultBusinessId
        val normalized = if (trimmed.contains(" ")) {
            trimmed.lowercase().replace(" ", "_")
        } else {
            trimmed
        }
        return sanitizeFirestoreId(normalized).ifBlank { defaultBusinessId }
    }

    private fun sanitizeFirestoreId(raw: String): String {
        return raw
            .trim()
            .replace("/", "_")
            .replace("#", "_")
            .replace("?", "_")
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
