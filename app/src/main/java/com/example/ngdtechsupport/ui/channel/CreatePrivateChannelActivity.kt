package com.example.ngdtechsupport.ui.channel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.databinding.ActivityCreatePrivateChannelBinding
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth

class CreatePrivateChannelActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private lateinit var binding: ActivityCreatePrivateChannelBinding
    private val channelViewModel: ChannelViewModel by viewModels()
    
    private lateinit var companyId: String
    private lateinit var businessId: String
    private lateinit var userRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePrivateChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getStringExtra("companyId") ?: DEFAULT_COMPANY_ID
        businessId = intent.getStringExtra("businessId") ?: DEFAULT_BUSINESS_ID
        userRole = intent.getStringExtra("userRole") ?: "CLIENT"

        setupToolbar()
        setupCreateButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupCreateButton() {
        binding.btnCreateChannel.setOnClickListener {
            createPrivateChannel()
        }
    }

    private fun createPrivateChannel() {
        val channelName = binding.etChannelName.text.toString().trim()
        val channelDescription = binding.etChannelDescription.text.toString().trim()
        
        if (channelName.isEmpty()) {
            Toast.makeText(this, "El nombre del canal es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate channel ID from name and user ID
        val channelId = "private_${currentUserId}_${System.currentTimeMillis()}"
        
        channelViewModel.createPrivateChannel(
            companyId = companyId,
            channelId = channelId,
            adminUid = currentUserId,
            memberUid = currentUserId,
            name = channelName,
            description = channelDescription
        )
    }

    private fun observeViewModel() {
        channelViewModel.privateChannelCreated.observe(this) { result ->
            if (result == null) return@observe

            if (result.created && result.channelId.isNotEmpty()) {
                Toast.makeText(this, "Canal privado creado exitosamente", Toast.LENGTH_SHORT).show()
                
                // Navigate to chat
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("companyId", companyId)
                    putExtra("businessId", businessId)
                    putExtra("userRole", userRole)
                    putExtra("channelId", result.channelId)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    result.errorMessage.ifEmpty { "No se pudo crear el canal privado" },
                    Toast.LENGTH_SHORT
                ).show()
            }

            channelViewModel.clearPrivateChannelState()
        }
    }
}