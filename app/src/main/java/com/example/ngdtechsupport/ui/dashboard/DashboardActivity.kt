package com.example.ngdtechsupport.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.lifecycle.Observer

import com.example.ngdtechsupport.ui.auth.LoginActivity
import com.example.ngdtechsupport.R

import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textView = findViewById<TextView>(R.id.tvApps)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)

        // Configuramos el RecyclerView con un LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Creamos el adapter con una lista vacía
        val adapter = AppAdapter(emptyList())
        recyclerView.adapter = adapter

        // Observamos el estado del ViewModel
        viewModel.uiState.observe(this, Observer { state ->
            when (state) {
                is DashboardUiState.Loading -> {
                    textView.text = "Cargando..."
                    adapter.updateApps(emptyList())
                }
                is DashboardUiState.Success -> {
                    textView.text = "Tus aplicaciones:"
                    adapter.updateApps(state.apps)
                }
                is DashboardUiState.Empty -> {
                    textView.text = "No tienes apps asignadas"
                    adapter.updateApps(emptyList())
                }
                is DashboardUiState.Error -> {
                    textView.text = state.message
                    adapter.updateApps(emptyList())
                }
            }
        })

        // Añadimos la observación del rol
        val roleTextView = findViewById<TextView>(R.id.tvRole)
        viewModel.userRole.observe(this, Observer { role ->
        // Por ejemplo, mostrar el rol
            roleTextView.text = "Rol: $role"

        // Aquí podrías mostrar/ocultar botones según el rol
            // if (role == "admin") { ... }
        })

        // Pedimos al ViewModel que cargue las apps del usuario actual
        viewModel.loadAppsForCurrentUser()

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}