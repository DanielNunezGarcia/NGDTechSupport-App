package com.example.ngdtechsupport.ai

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.databinding.ActivityAiChatBinding
import com.google.firebase.auth.FirebaseAuth

class AiChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiChatBinding
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var adapter: AiChatAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var companyId = "NGDStudios"
    private var currentUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityAiChatBinding.inflate(layoutInflater)
            setContentView(binding.root)

            currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            
            companyId = intent.getStringExtra("companyId") ?: companyId

            setupRecycler()
            setupSendButton()
            setupTransferButton()

            viewModel.sendMessage(companyId, "Hola, necesito ayuda")

            viewModel.messages.observe(this) { messages ->
                try {
                    adapter.submitList(messages.toList())
                    if (messages.isNotEmpty()) {
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AiChatActivity", "Error in messages observer", e)
                }
            }

            viewModel.isLoading.observe(this) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.buttonSend.isEnabled = !isLoading
                } catch (e: Exception) {
                    android.util.Log.e("AiChatActivity", "Error in isLoading observer", e)
                }
            }

            viewModel.error.observe(this) { error ->
                try {
                    error?.let {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AiChatActivity", "Error in error observer", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AiChatActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error al iniciar chat AI", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecycler() {
        adapter = AiChatAdapter(currentUserId)
        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }

    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            try {
                val text = binding.editTextMessage.text.toString()
                if (text.isBlank()) return@setOnClickListener

                viewModel.sendMessage(companyId, text)
                binding.editTextMessage.text.clear()
            } catch (e: Exception) {
                android.util.Log.e("AiChatActivity", "Error in send button click", e)
            }
        }
    }

    private fun setupTransferButton() {
        binding.buttonTransferHuman.setOnClickListener {
            try {
                viewModel.transferToHuman(companyId, currentUserId) { channelId ->
                    try {
                        Toast.makeText(this, "Conversation transferred to human support", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        android.util.Log.e("AiChatActivity", "Error in transfer callback", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AiChatActivity", "Error in transfer button click", e)
            }
        }
    }
}
