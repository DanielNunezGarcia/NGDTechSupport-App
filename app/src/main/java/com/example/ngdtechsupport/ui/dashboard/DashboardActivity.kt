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
                if (success) {
                    val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
                    intent.putExtra("companyId", currentCompanyId)
                    intent.putExtra("businessId", "")
                    intent.putExtra("channelId", currentPrivateChannelId)
                    startActivity(intent)
                } else {
                    Log.e("DashboardActivity", "Failed to create private channel")
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error in privateChannelCreated observer", e)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AppAdapter(emptyList()) { app, companyId ->
            try {
                val intent = Intent(
                    this,
                    com.example.ngdtechsupport.ui.activity.AppDetailActivity::class.java
                )
                intent.putExtra("appId", app.id)
                intent.putExtra("companyId", companyId)
                intent.putExtra("businessId", app.id)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening app detail", e)
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

                currentCompanyId = state.companyId
                Log.d("DashboardActivity", "currentCompanyId: $currentCompanyId")
                if (state.apps.isNotEmpty()) {
                    currentBusinessId = state.apps.first().id
                    Log.d("DashboardActivity", "currentBusinessId set to first app id: $currentBusinessId")
                } else {
                    currentBusinessId = state.businessId
                    Log.d("DashboardActivity", "No apps, currentBusinessId set to state.businessId: $currentBusinessId")
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error in UI observer", e)
            }
        }

        btnChatGlobal.setOnClickListener {
            try {
                Log.d("DashboardActivity", "Chat button clicked. currentCompanyId: $currentCompanyId, currentBusinessId: $currentBusinessId")
                if (currentCompanyId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot open chat: currentCompanyId is empty")
                    return@setOnClickListener
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
            }
        }

        btnUpdatesGlobal.setOnClickListener {
            try {
                Log.d("DashboardActivity", "Updates button clicked. currentCompanyId: $currentCompanyId, currentBusinessId: $currentBusinessId")
                if (currentCompanyId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot open updates: currentCompanyId is empty")
                    return@setOnClickListener
                }
                if (currentBusinessId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot open updates: currentBusinessId is empty")
                    Toast.makeText(this, "Error: No hay negocio seleccionado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val intent = Intent(this, com.example.ngdtechsupport.ui.updates.UpdatesActivity::class.java)
                intent.putExtra("companyId", currentCompanyId)
                intent.putExtra("businessId", currentBusinessId)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening updates", e)
            }
        }

        btnAiConfig.setOnClickListener {
            try {
                val intent = Intent(this, com.example.ngdtechsupport.ui.admin.AiConfigActivity::class.java)
                intent.putExtra("companyId", currentCompanyId.ifEmpty { "NGDStudios" })
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening AI config", e)
            }
        }

        btnChannels.setOnClickListener {
            try {
                val intent = Intent(this, ChannelActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error opening channels", e)
            }
        }

        btnCreatePrivateChannel.setOnClickListener {
            try {
                Log.d("DashboardActivity", "Create private channel button clicked. currentCompanyId: $currentCompanyId")
                if (currentCompanyId.isEmpty()) {
                    Log.e("DashboardActivity", "Cannot create private channel: currentCompanyId is empty")
                    return@setOnClickListener
                }
                currentPrivateChannelId = "private_${currentUserId}_${System.currentTimeMillis()}"
                Log.d("DashboardActivity", "Creating private channel with ID: $currentPrivateChannelId")
                channelViewModel.createPrivateChannel(
                    companyId = currentCompanyId,
                    channelId = currentPrivateChannelId,
                    adminUid = currentUserId,
                    memberUid = currentUserId
                )
                // Navigation will be handled by observer on privateChannelCreated
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error initiating private channel creation: ${e.message}", e)
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
