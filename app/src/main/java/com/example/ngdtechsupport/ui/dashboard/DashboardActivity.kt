package com.example.ngdtechsupport.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.ngdtechsupport.ui.channel.ChannelViewModel
import com.example.ngdtechsupport.ui.auth.LoginActivity
import com.example.ngdtechsupport.R

import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: AppAdapter
    private lateinit var channelViewModel: ChannelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textView = findViewById<TextView>(R.id.tvApps)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        val roleTextView = findViewById<TextView>(R.id.tvRole)
        val userInfoTextView = findViewById<TextView>(R.id.tvUserInfo)

        channelViewModel = ViewModelProvider(this)[ChannelViewModel::class.java]
        val btnCreatePrivateChannel = findViewById<Button>(R.id.btnCreatePrivateChannel)
        btnCreatePrivateChannel.setOnClickListener {

            channelViewModel.createPrivateChannel(
                companyId = "NGDStudios",
                channelId = "private_${System.currentTimeMillis()}",
                adminUid = "adminUid01",
                memberUid = "clientUid02"
            )
        }

        // Configuramos el RecyclerView con un LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Creamos el adapter con una lista vacía
        adapter = AppAdapter(emptyList())
        recyclerView.adapter = adapter

        // Observamos el estado del ViewModel (TODO en uno)
        viewModel.uiState.observe(this) { state ->
            // Mostrar/ocultar loading si es necesario
            if (state.isLoading) {
                textView.text = "Cargando..."
                adapter.updateApps(emptyList())
            } else if (state.hasError) {
                textView.text = state.errorMessage ?: "Error desconocido"
                adapter.updateApps(emptyList())
            } else if (state.isEmpty) {
                textView.text = "No tienes apps asignadas"
                adapter.updateApps(emptyList())
            } else if (state.isSuccess) {
                textView.text = "Tus aplicaciones:"
                adapter.updateApps(state.apps)
            }

            // Mostrar rol
            if (state.userRole.isNotEmpty()) {
                roleTextView.text = "Rol: ${state.userRole}"
            }

            // Mostrar información del usuario
            if (state.companyName.isNotEmpty()) {
                userInfoTextView.text = "${state.userName} - ${state.companyName}"
            } else if (state.userName.isNotEmpty()) {
                userInfoTextView.text = state.userName
            }
        }

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