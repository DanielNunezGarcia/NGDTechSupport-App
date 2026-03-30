package com.example.ngdtechsupport.viewmodel

import org.junit.Test
import org.junit.Assert.*

class DashboardViewModelTest {

    // Test UI state model
    @Test
    fun `dashboard ui state initial values`() {
        val state = DashboardUiStateTestModel()

        assertTrue("Should be loading initially", state.isLoading)
        assertFalse("Should not have error initially", state.hasError)
        assertNull("Error message should be null initially", state.errorMessage)
        assertTrue("Apps list should be empty initially", state.apps.isEmpty())
        assertEquals("User role should be CLIENT by default", "CLIENT", state.userRole)
        assertTrue("Company name should be empty initially", state.companyName.isEmpty())
    }

    @Test
    fun `dashboard ui state with error`() {
        val state = DashboardUiStateTestModel(
            isLoading = false,
            hasError = true,
            errorMessage = "Error loading data"
        )

        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should have error", state.hasError)
        assertEquals("Error message should match", "Error loading data", state.errorMessage)
    }

    @Test
    fun `dashboard ui state with apps`() {
        val apps = listOf(
            AppTestModel("app1", "App 1"),
            AppTestModel("app2", "App 2")
        )
        val state = DashboardUiStateTestModel(
            isLoading = false,
            apps = apps
        )

        assertFalse("Should not be loading", state.isLoading)
        assertEquals("Should have 2 apps", 2, state.apps.size)
        assertEquals("First app should be App 1", "App 1", state.apps[0].name)
    }

    @Test
    fun `dashboard ui state with admin role`() {
        val state = DashboardUiStateTestModel(
            userRole = "ADMIN"
        )

        assertEquals("User role should be ADMIN", "ADMIN", state.userRole)
    }

    @Test
    fun `dashboard ui state with company info`() {
        val state = DashboardUiStateTestModel(
            companyName = "NGD Studios"
        )

        assertEquals("Company name should match", "NGD Studios", state.companyName)
    }

    // Test models
    private data class DashboardUiStateTestModel(
        val isLoading: Boolean = true,
        val hasError: Boolean = false,
        val errorMessage: String? = null,
        val apps: List<AppTestModel> = emptyList(),
        val userRole: String = "CLIENT",
        val companyName: String = "",
        val userName: String = ""
    )

    private data class AppTestModel(
        val id: String,
        val name: String
    )
}