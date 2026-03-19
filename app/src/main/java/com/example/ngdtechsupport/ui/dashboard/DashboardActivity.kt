package com.example.ngdtechsupport.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels

import com.example.ngdtechsupport.ui.channel.ChannelViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textView = findViewById<TextView>(R.id.tvApps)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        val roleTextView = findViewById<TextView>(R.id.tvRole)
        val userInfoTextView = findViewById<TextView>(R.id.tvUserInfo)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        channelViewModel = ChannelViewModel()

        val btnCreatePrivateChannel = findViewById<Button>(R.id.btnCreatePrivateChannel)
        btnCreatePrivateChannel.setOnClickListener {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentCompanyId.isEmpty()) {
                Toast.makeText(this, "Cargando datos...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentUid?.let { uid ->
                channelViewModel.createPrivateChannel(
                    companyId = currentCompanyId,
                    channelId = "private_$uid",
                    adminUid = uid,
                    memberUid = uid
                )
                Toast.makeText(this, "Canal privado creado", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AppAdapter(emptyList()) { app, companyId ->
            val intent = Intent(
                this,
                com.example.ngdtechsupport.ui.activity.AppDetailActivity::class.java
            )
            intent.putExtra("appId", app.id)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", app.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        viewModel.uiState.observe(this) { state ->
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
            }

            if (state.companyName.isNotEmpty()) {
                userInfoTextView.text = "${state.userName} - ${state.companyName}"
            } else if (state.userName.isNotEmpty()) {
                userInfoTextView.text = state.userName
            }

            currentCompanyId = state.companyId
        }

        viewModel.loadAppsForCurrentUser()

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val btnChatGlobal = findViewById<Button>(R.id.btnChat)
        val btnUpdatesGlobal = findViewById<Button>(R.id.btnUpdates)

        btnChatGlobal.setOnClickListener {
            val companyId = viewModel.uiState.value?.companyId ?: ""
            if (companyId.isEmpty()) {
                Toast.makeText(this, "Cargando datos...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(
                this,
                com.example.ngdtechsupport.ui.chat.ChatActivity::class.java
            )
            intent.putExtra("companyId", companyId)
            startActivity(intent)
        }

        btnUpdatesGlobal.setOnClickListener {
            val companyId = viewModel.uiState.value?.companyId ?: ""
            if (companyId.isEmpty()) {
                Toast.makeText(this, "Cargando datos...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(
                this,
                com.example.ngdtechsupport.ui.updates.UpdatesActivity::class.java
            )
            intent.putExtra("companyId", companyId)
            startActivity(intent)
        }

        val btnAiConfig = findViewById<Button>(R.id.btnAiConfig)
        
        viewModel.uiState.observe(this) { state ->
            btnAiConfig.visibility = if (state.userRole == "ADMIN") {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }

        btnAiConfig.setOnClickListener {
            val companyId = viewModel.uiState.value?.companyId ?: "NGDStudios"
            val intent = Intent(
                this,
                com.example.ngdtechsupport.ui.admin.AiConfigActivity::class.java
            )
            intent.putExtra("companyId", companyId)
            startActivity(intent)
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
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
            }
    }
}
