package com.example.ngdtechsupport.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.example.ngdtechsupport.ui.updates.UpdatesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: AppDetailViewModel
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_detail)

        val tvBusinessName = findViewById<TextView>(R.id.tvBusinessName)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        val tvSupportType = findViewById<TextView>(R.id.tvSupportType)
        val tvLastUpdate = findViewById<TextView>(R.id.tvLastUpdate)

        val btnChat = findViewById<Button>(R.id.btnChat)
        val btnUpdates = findViewById<Button>(R.id.btnUpdates)

        val companyId = intent.getStringExtra("companyId") ?: "NGDStudios"
        val businessId = intent.getStringExtra("businessId") ?: intent.getStringExtra("appId") ?: ""

        if (businessId.isEmpty()) {
            tvBusinessName.text = "Error: No se encontró el negocio"
            btnChat.isEnabled = false
            btnUpdates.isEnabled = false
            return
        }

        viewModel = ViewModelProvider(this)[AppDetailViewModel::class.java]

        viewModel.business.observe(this) { business ->
            tvBusinessName.text = business.name
            tvStatus.text = business.status

            progressBar.progress = business.progress
            tvProgress.text = "${business.progress}%"

            tvVersion.text = "Versión: ${business.version}"
            tvSupportType.text = "Soporte: ${business.supportType}"
            tvLastUpdate.text = "Última actualización: ${business.lastUpdate}"
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                tvBusinessName.text = "Error al cargar"
                tvStatus.text = error
            }
        }

        viewModel.loadBusiness(companyId, businessId)

        val channelId = "${businessId}_support"

        btnChat.setOnClickListener {
            ensureChannelExists(companyId, channelId) { success ->
                if (success) {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("companyId", companyId)
                    intent.putExtra("businessId", businessId)
                    intent.putExtra("channelId", channelId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Error al abrir chat", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnUpdates.setOnClickListener {
            val intent = Intent(this, UpdatesActivity::class.java)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)
            startActivity(intent)
        }
    }

    private fun ensureChannelExists(companyId: String, channelId: String, onComplete: (Boolean) -> Unit) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val channelRef = firestore.collection("companies")
                    .document(companyId)
                    .collection("channels")
                    .document(channelId)

                val channelDoc = channelRef.get().await()

                if (!channelDoc.exists()) {
                    val channelData = hashMapOf(
                        "name" to "Soporte",
                        "createdAt" to Timestamp.now(),
                        "isArchived" to false,
                        "pinned" to false,
                        "members" to mapOf(
                            currentUid to mapOf("role" to "member")
                        ),
                        "mutedUsers" to emptyMap<String, Boolean>(),
                        "unreadCount" to mapOf(
                            currentUid to 0L
                        ),
                        "unread_admin" to 0,
                        "unread_client" to 0
                    )
                    channelRef.set(channelData).await()
                }

                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}
