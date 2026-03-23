package com.example.ngdtechsupport.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.util.Log


import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.lifecycle.Observer

import com.example.ngdtechsupport.ui.channel.ChannelViewModel
import com.example.ngdtechsupport.ui.channel.ChannelActivity
import com.example.ngdtechsupport.ui.auth.LoginActivity
import com.example.ngdtechsupport.R

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: AppAdapter
    private lateinit var channelViewModel: ChannelViewModel
    private var currentCompanyId: String = ""
    private var currentBusinessId: String = ""
    private var currentUserId: String = ""
    private var currentPrivateChannelId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textView = findViewById<TextView>(R.id.tvApps)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        val roleTextView = findViewById<TextView>(R.id.tvRole)
        val userInfoTextView = findViewById<TextView>(R.id.tvUserInfo)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        channelViewModel = ChannelViewModel()
        
        channelViewModel.privateChannelCreated.observe(this, Observer { success ->
            try {
                Log.d("DashboardActivity", "Private channel created observer: success=$success, currentCompanyId=$currentCompanyId, currentPrivateChannelId=$currentPrivateChannelId")
                if (success) {
                    if (currentCompanyId.isEmpty()) {
                        Log.w("DashboardActivity", "currentCompanyId is empty, using default")
                        currentCompanyId = "NGDStudios"
                    }
                    if (currentPrivateChannelId.isEmpty()) {
                        Log.e("DashboardActivity", "currentPrivateChannelId is empty, cannot navigate")
                        Toast.makeText(this, "Error: ID de canal vacío", Toast.LENGTH_SHORT).show()
                        return@Observer
                    }
                    val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
                    intent.putExtra("companyId", currentCompanyId)
                    intent.putExtra("businessId", "")
                    intent.putExtra("channelId", currentPrivateChannelId)
                    startActivity(intent)
                } else {
                    Log.e("DashboardActivity", "Failed to create private channel")
                    Toast.makeText(this, "Error al crear canal privado. Verifique los permisos.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error in privateChannelCreated observer", e)
                Toast.makeText(this, "Error en observador de canal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AppAdapter(emptyList()) { app, companyId ->
            try {
                Log.d("DashboardActivity", "App clicked: ${app.id}, companyId: $companyId")
                val intent = Intent(
                    this,
                    com.example.ngdtechsupport.ui.activity.AppDetailActivity::class.java
                )
                intent.putExtra("appId", app.id)
                intent.putExtra("companyId", companyId.ifEmpty { "NGDStudios" })
                intent.putExtra("businessId", app.id)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening app detail", e)
                Toast.makeText(this, "Error al abrir detalle: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        val btnChatGlobal = findViewById<Button>(R.id.btnChat)
        val btnUpdatesGlobal = findViewById<Button>(R.id.btnUpdates)
        val btnChannels = findViewById<Button>(R.id.btnChannels)
        val btnAiConfig = findViewById<Button>(R.id.btnAiConfig)
        val btnCreatePrivateChannel = findViewById<Button>(R.id.btnCreatePrivateChannel)

        btnAiConfig.visibility = View.GONE

        viewModel.uiState.observe(this) { state ->
            try {
                Log.d("DashboardActivity", "UI State: isLoading=${state.isLoading}, hasError=${state.hasError}, isEmpty=${state.isEmpty}, isSuccess=${state.isSuccess}")
                Log.d("DashboardActivity", "Apps count: ${state.apps.size}")
                if (state.apps.isNotEmpty()) {
                    Log.d("DashboardActivity", "First app: ${state.apps.first()}")
                }
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

                if (state.userRole.isNotEmpty()) {
                    roleTextView.text = "Rol: ${state.userRole}"
                    btnAiConfig.visibility = if (state.userRole == "ADMIN") View.VISIBLE else View.GONE
                }

                if (state.companyName.isNotEmpty()) {
                    userInfoTextView.text = "${state.userName} - ${state.companyName}"
                } else if (state.userName.isNotEmpty()) {
                    userInfoTextView.text = state.userName
                }

                currentCompanyId = state.companyId.ifEmpty { "NGDStudios" }
                Log.d("DashboardActivity", "currentCompanyId set to: $currentCompanyId")
                if (state.apps.isNotEmpty()) {
                    currentBusinessId = state.apps.first().id
                    Log.d("DashboardActivity", "currentBusinessId set to first app id: $currentBusinessId")
                } else {
                    currentBusinessId = state.businessId.ifEmpty { "restaurante_madrid" }
                    Log.d("DashboardActivity", "No apps, currentBusinessId set to: $currentBusinessId")
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error in UI observer", e)
                Toast.makeText(this, "Error al actualizar UI: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnChatGlobal.setOnClickListener {
            try {
                Log.d("DashboardActivity", "Chat button clicked. currentCompanyId: $currentCompanyId, currentBusinessId: $currentBusinessId")
                if (currentCompanyId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot open chat: currentCompanyId is empty, using default")
                    currentCompanyId = "NGDStudios"
                }
                val channelId = if (currentBusinessId.isNotEmpty()) "${currentBusinessId}_support" else "default_support"
                Log.d("DashboardActivity", "Opening ChatActivity with channelId: $channelId")
                val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
                intent.putExtra("companyId", currentCompanyId)
                intent.putExtra("businessId", currentBusinessId)
                intent.putExtra("channelId", channelId)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening chat", e)
                Toast.makeText(this, "Error al abrir chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdatesGlobal.setOnClickListener {
            try {
                Log.d("DashboardActivity", "Updates button clicked. currentCompanyId: $currentCompanyId, currentBusinessId: $currentBusinessId")
                if (currentCompanyId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot open updates: currentCompanyId is empty, using default")
                    currentCompanyId = "NGDStudios"
                }
                if (currentBusinessId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot open updates: currentBusinessId is empty, using default")
                    currentBusinessId = "restaurante_madrid"
                }
                val intent = Intent(this, com.example.ngdtechsupport.ui.updates.UpdatesActivity::class.java)
                intent.putExtra("companyId", currentCompanyId)
                intent.putExtra("businessId", currentBusinessId)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening updates", e)
                Toast.makeText(this, "Error al abrir actualizaciones: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnAiConfig.setOnClickListener {
            try {
                val intent = Intent(this, com.example.ngdtechsupport.ui.admin.AiConfigActivity::class.java)
                intent.putExtra("companyId", currentCompanyId.ifEmpty { "NGDStudios" })
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening AI config", e)
                Toast.makeText(this, "Error al abrir configuración AI: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnChannels.setOnClickListener {
            try {
                val intent = Intent(this, ChannelActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening channels", e)
                Toast.makeText(this, "Error al abrir canales: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnCreatePrivateChannel.setOnClickListener {
            try {
                Log.d("DashboardActivity", "Create private channel button clicked. currentCompanyId: $currentCompanyId, currentUserId: $currentUserId")
                if (currentCompanyId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot create private channel: currentCompanyId is empty, using default")
                    currentCompanyId = "NGDStudios"
                }
                if (currentUserId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot create private channel: currentUserId is empty")
                    Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                currentPrivateChannelId = "private_${currentUserId}_${System.currentTimeMillis()}"
                Log.d("DashboardActivity", "Creating private channel with ID: $currentPrivateChannelId")
                Toast.makeText(this, "Creando canal privado...", Toast.LENGTH_SHORT).show()
                channelViewModel.createPrivateChannel(
                    companyId = currentCompanyId,
                    channelId = currentPrivateChannelId,
                    adminUid = currentUserId,
                    memberUid = currentUserId
                )
                // Navigation will be handled by observer on privateChannelCreated
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error initiating private channel creation: ${e.message}", e)
                Toast.makeText(this, "Error al crear canal privado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }



        viewModel.loadAppsForCurrentUser()

        logoutButton.setOnClickListener {
            try {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error during logout", e)
                Toast.makeText(this, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                try {
                    if (!task.isSuccessful) {
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(
                            mapOf("fcmToken" to token),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                } catch (e: Exception) {
                    Log.e("DashboardActivity", "Error saving FCM token", e)
                }
            }
    }
}