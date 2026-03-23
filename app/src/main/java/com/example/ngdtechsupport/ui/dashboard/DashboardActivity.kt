package com.example.ngdtechsupport.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.auth.LoginActivity
import com.example.ngdtechsupport.ui.channel.ChannelActivity
import com.example.ngdtechsupport.ui.channel.ChannelViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private val viewModel: DashboardViewModel by viewModels()
    private val channelViewModel: ChannelViewModel by viewModels()
    private lateinit var adapter: AppAdapter

    private var currentUserId: String = ""
    private var currentCompanyId: String = ""
    private var currentBusinessId: String = ""
    private var currentUserRole: String = "CLIENT"
    private var privateChannelId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val tvApps = findViewById<TextView>(R.id.tvApps)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val tvUserInfo = findViewById<TextView>(R.id.tvUserInfo)
        val rvApps = findViewById<RecyclerView>(R.id.rvApps)

        val btnChat = findViewById<Button>(R.id.btnChat)
        val btnUpdates = findViewById<Button>(R.id.btnUpdates)
        val btnChannels = findViewById<Button>(R.id.btnChannels)
        val btnAiConfig = findViewById<Button>(R.id.btnAiConfig)
        val btnCreatePrivateChannel = findViewById<Button>(R.id.btnCreatePrivateChannel)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (currentUserId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        currentCompanyId = DEFAULT_COMPANY_ID
        currentBusinessId = DEFAULT_BUSINESS_ID

        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.setHasFixedSize(true)

        adapter = AppAdapter(emptyList()) { app, companyId ->
            val intent = Intent(this, com.example.ngdtechsupport.ui.activity.AppDetailActivity::class.java)
            intent.putExtra("appId", app.id)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", app.id)
            startActivity(intent)
        }
        rvApps.adapter = adapter

        btnAiConfig.visibility = View.GONE

        viewModel.uiState.observe(this) { state ->
            when {
                state.isLoading -> {
                    tvApps.text = "Cargando..."
                    adapter.updateApps(emptyList())
                }
                state.hasError -> {
                    tvApps.text = state.errorMessage ?: "Error"
                    adapter.updateApps(emptyList())
                }
                state.apps.isEmpty() -> {
                    tvApps.text = "No tienes apps asignadas"
                    adapter.updateApps(emptyList())
                }
                else -> {
                    tvApps.text = "Tus aplicaciones:"
                    adapter.updateApps(state.apps)
                }
            }

            tvRole.text = "Rol: ${state.userRole.ifEmpty { "CLIENT" }}"
            currentUserRole = state.userRole.ifEmpty { "CLIENT" }
            tvUserInfo.text = if (state.companyName.isNotEmpty()) {
                "${state.userName} - ${state.companyName}"
            } else {
                state.userName
            }

            currentCompanyId = state.companyId.ifEmpty { DEFAULT_COMPANY_ID }
            currentBusinessId = when {
                state.apps.isNotEmpty() -> state.apps.first().id
                state.businessId.isNotEmpty() -> state.businessId
                else -> DEFAULT_BUSINESS_ID
            }

            btnAiConfig.visibility = if (state.userRole == "ADMIN") View.VISIBLE else View.GONE
        }

        channelViewModel.privateChannelCreated.observe(this) { success ->
            if (success && privateChannelId.isNotEmpty()) {
                val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
                intent.putExtra("companyId", currentCompanyId)
                intent.putExtra("businessId", currentBusinessId)
                intent.putExtra("userRole", currentUserRole)
                intent.putExtra("channelId", privateChannelId)
                startActivity(intent)
                channelViewModel.clearPrivateChannelState()
            }
        }

        btnChat.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
            val safeCompanyId = currentCompanyId.ifEmpty { DEFAULT_COMPANY_ID }
            val safeBusinessId = currentBusinessId.ifEmpty { DEFAULT_BUSINESS_ID }
            intent.putExtra("companyId", safeCompanyId)
            intent.putExtra("businessId", safeBusinessId)
            intent.putExtra("userRole", currentUserRole)
            intent.putExtra("channelId", "${safeBusinessId}_support")
            startActivity(intent)
        }

        btnUpdates.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            runCatching {
                val intent = Intent(this, com.example.ngdtechsupport.ui.updates.UpdatesActivity::class.java)
                intent.putExtra("companyId", currentCompanyId.ifEmpty { DEFAULT_COMPANY_ID })
                intent.putExtra("businessId", currentBusinessId.ifEmpty { DEFAULT_BUSINESS_ID })
                intent.putExtra("userRole", currentUserRole)
                startActivity(intent)
            }.onFailure {
                Toast.makeText(this, "No se pudo abrir Updates", Toast.LENGTH_SHORT).show()
            }
        }

        btnChannels.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val intent = Intent(this, ChannelActivity::class.java)
            intent.putExtra("companyId", currentCompanyId.ifEmpty { DEFAULT_COMPANY_ID })
            startActivity(intent)
        }

        btnCreatePrivateChannel.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val companyId = currentCompanyId.ifEmpty { DEFAULT_COMPANY_ID }
            privateChannelId = "private_$currentUserId"
            runCatching {
                channelViewModel.createPrivateChannel(
                    companyId = companyId,
                    channelId = privateChannelId,
                    adminUid = currentUserId,
                    memberUid = currentUserId
                )
            }.onFailure {
                Toast.makeText(this, "No se pudo crear el canal privado", Toast.LENGTH_SHORT).show()
            }
        }

        btnAiConfig.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val intent = Intent(this, com.example.ngdtechsupport.ui.admin.AiConfigActivity::class.java)
            intent.putExtra("companyId", currentCompanyId.ifEmpty { DEFAULT_COMPANY_ID })
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        viewModel.loadAppsForCurrentUser()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
        }
    }

    private fun isSessionValid(): Boolean {
        if (currentUserId.isNotEmpty()) return true

        Toast.makeText(this, "Sesion invalida. Inicia sesion de nuevo.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        return false
    }
}
