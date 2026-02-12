package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.AppModel

    // Interfaz que define qué operaciones ofrece el repositorio de apps
    interface AppRepository {
        suspend fun getAppsForUser(userId: String): List<AppModel>
    }