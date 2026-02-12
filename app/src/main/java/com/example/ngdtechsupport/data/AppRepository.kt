package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.AppModel

// Interfaz que define qué operaciones ofrece el repositorio de apps
interface AppRepository {

    // Apps de un usuario concreto (cliente)
    suspend fun getAppsForUser(userId: String): List<AppModel>

    // Todas las apps (para admin)
    suspend fun getAllApps(): List<AppModel>
}