package com.example.ngdtechsupport.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.ngdtechsupport.utils.AnalyticsHelper

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardActivity"
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
    private var loginTracked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        AnalyticsHelper.screenView("DashboardActivity")

        val tvApps = findViewById<TextView>(R.id.tvApps)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val tvUserInfo = findViewById<TextView>(R.id.tvUserInfo)
        val rvApps = findViewById<RecyclerView>(R.id.rvApps)
        val skeletonDashboard = findViewById<View>(R.id.skeletonDashboard)

        val btnChat = findViewById<Button>(R.id.btnChat)
        val btnUpdates = findViewById<Button>(R.id.btnUpdates)
        val btnChannels = findViewById<Button>(R.id.btnChannels)
        val btnAiConfig = findViewById<Button>(R.id.btnAiConfig)
        val btnCreatePrivateChannel = findViewById<Button>(R.id.btnCreatePrivateChannel)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (currentUserId.isEmpty()) {
            Log.w(TAG, "No authenticated user in DashboardActivity, redirecting to Login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        currentCompanyId = DEFAULT_COMPANY_ID
        currentBusinessId = DEFAULT_BUSINESS_ID

        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.setHasFixedSize(true)
        rvApps.setItemViewCacheSize(10)
        rvApps.setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 16)
        })

        adapter = AppAdapter(emptyList()) { app, companyId ->
            val intent = Intent(this, com.example.ngdtechsupport.ui.activity.AppDetailActivity::class.java)
            intent.putExtra("appId", app.id)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", app.id)
            startActivity(intent)
        }
        rvApps.adapter = adapter
        skeletonDashboard.visibility = View.VISIBLE
        rvApps.visibility = View.GONE

        btnAiConfig.visibility = View.GONE

        viewModel.uiState.observe(this) { state ->
            Log.d(TAG, "Dashboard state changed role=${state.userRole} apps=${state.apps.size} companyId=${state.companyId} businessId=${state.businessId}")

            when {
                state.isLoading -> {
                    tvApps.text = "Cargando..."
                    skeletonDashboard.visibility = View.VISIBLE
                    rvApps.visibility = View.GONE
                    adapter.updateApps(emptyList())
                }
                state.hasError -> {
                    tvApps.text = state.errorMessage ?: "Error"
                    skeletonDashboard.visibility = View.GONE
                    rvApps.visibility = View.VISIBLE
                    adapter.updateApps(emptyList())
                }
                state.apps.isEmpty() -> {
                    tvApps.text = "No tienes apps asignadas"
                    skeletonDashboard.visibility = View.GONE
                    rvApps.visibility = View.VISIBLE
                    adapter.updateApps(emptyList())
                }
                else -> {
                    tvApps.text = "Tus aplicaciones:"
                    skeletonDashboard.visibility = View.GONE
                    rvApps.visibility = View.VISIBLE
                    adapter.updateApps(state.apps)
                }
            }

            tvRole.text = "Rol: ${state.userRole.ifEmpty { "CLIENT" }}"
            currentUserRole = state.userRole.ifEmpty { "CLIENT" }
            if (!loginTracked) {
                AnalyticsHelper.loginSuccess(currentUserRole)
                loginTracked = true
            }
            tvUserInfo.text = if (state.companyName.isNotEmpty()) {
                "${state.userName} - ${state.companyName}"
            } else {
                state.userName
            }

            currentCompanyId = resolveCompanyId(state.companyId)
            val appBusinessId = state.apps.firstOrNull()?.id.orEmpty()
            currentBusinessId = resolveBusinessId(state.businessId.ifBlank { appBusinessId })

            Log.d(TAG, "Resolved navigation scope companyId=$currentCompanyId businessId=$currentBusinessId role=$currentUserRole")

            btnAiConfig.visibility = if (state.userRole == "ADMIN") View.VISIBLE else View.GONE
        }

        channelViewModel.privateChannelCreated.observe(this) { result ->
            if (result == null) return@observe

            if (result.created && result.channelId.isNotEmpty()) {
                val safeCompanyId = resolveCompanyId(currentCompanyId)
                val safeBusinessId = resolveBusinessId(currentBusinessId)
                Log.d(TAG, "Opening private chat with channelId=${result.channelId} companyId=$safeCompanyId businessId=$safeBusinessId")
                AnalyticsHelper.privateChannelCreated(result.channelId)
                val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
                intent.putExtra("companyId", safeCompanyId)
                intent.putExtra("businessId", safeBusinessId)
                intent.putExtra("userRole", currentUserRole)
                intent.putExtra("channelId", result.channelId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Toast.makeText(
                    this,
                    result.errorMessage.ifEmpty { "No se pudo crear el canal privado" },
                    Toast.LENGTH_SHORT
                ).show()
            }

            channelViewModel.clearPrivateChannelState()
        }

        btnChat.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val intent = Intent(this, com.example.ngdtechsupport.ui.chat.ChatActivity::class.java)
            val safeCompanyId = resolveCompanyId(currentCompanyId)
            val safeBusinessId = resolveBusinessId(currentBusinessId)
            Log.d(TAG, "Opening Chat with companyId=$safeCompanyId businessId=$safeBusinessId role=$currentUserRole")
            intent.putExtra("companyId", safeCompanyId)
            intent.putExtra("businessId", safeBusinessId)
            intent.putExtra("userRole", currentUserRole)
            intent.putExtra("channelId", "${safeBusinessId}_support")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnUpdates.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val safeCompanyId = resolveCompanyId(currentCompanyId)
            val safeBusinessId = resolveBusinessId(currentBusinessId)
            Log.d(TAG, "Opening Updates with companyId=$safeCompanyId businessId=$safeBusinessId role=$currentUserRole")
            runCatching {
                val intent = Intent(this, com.example.ngdtechsupport.ui.updates.UpdatesActivity::class.java)
                intent.putExtra("companyId", safeCompanyId)
                intent.putExtra("businessId", safeBusinessId)
                intent.putExtra("userRole", currentUserRole)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }.onFailure {
                Log.e(TAG, "Failed to open Updates", it)
                Toast.makeText(this, "No se pudo abrir Updates", Toast.LENGTH_SHORT).show()
            }
        }

        btnChannels.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val intent = Intent(this, ChannelActivity::class.java)
            val safeCompanyId = resolveCompanyId(currentCompanyId)
            val safeBusinessId = resolveBusinessId(currentBusinessId)
            Log.d(TAG, "Opening Channels with companyId=$safeCompanyId businessId=$safeBusinessId role=$currentUserRole")
            intent.putExtra("companyId", safeCompanyId)
            intent.putExtra("businessId", safeBusinessId)
            intent.putExtra("userRole", currentUserRole)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnCreatePrivateChannel.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val companyId = resolveCompanyId(currentCompanyId)
            val privateChannelId = "private_$currentUserId"
            runCatching {
                channelViewModel.createPrivateChannel(
                    companyId = companyId,
                    channelId = privateChannelId,
                    adminUid = currentUserId,
                    memberUid = currentUserId
                )
            }.onFailure {
                Log.e(TAG, "Failed to create private channel", it)
                Toast.makeText(this, "No se pudo crear el canal privado", Toast.LENGTH_SHORT).show()
            }
        }

        btnAiConfig.setOnClickListener {
            if (!isSessionValid()) return@setOnClickListener
            val intent = Intent(this, com.example.ngdtechsupport.ui.admin.AiConfigActivity::class.java)
            intent.putExtra("companyId", resolveCompanyId(currentCompanyId))
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

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (currentUserId.isNotEmpty()) return true

        Toast.makeText(this, "Sesion invalida. Inicia sesion de nuevo.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        return false
    }

    private fun resolveCompanyId(raw: String?): String {
        val normalized = raw
            .orEmpty()
            .trim()
            .ifBlank { DEFAULT_COMPANY_ID }
            .replace("/", "_")
            .replace("#", "_")
            .replace("?", "_")

        return if (normalized.equals(DEFAULT_COMPANY_ID, ignoreCase = true)) {
            DEFAULT_COMPANY_ID
        } else {
            normalized
        }
    }

    private fun resolveBusinessId(raw: String?): String {
        val base = raw.orEmpty().trim()
        if (base.isBlank()) return DEFAULT_BUSINESS_ID
        if (base.equals(DEFAULT_BUSINESS_ID, ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (base.equals("Restaurante Madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (base.equals("restaurante-madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID

        val normalized = if (base.contains(" ")) {
            base.lowercase().replace(" ", "_")
        } else {
            base
        }

        return normalized
            .replace("/", "_")
            .replace("#", "_")
            .replace("?", "_")
            .ifBlank { DEFAULT_BUSINESS_ID }
    }
}
