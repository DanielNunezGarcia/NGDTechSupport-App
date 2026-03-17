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
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        
        companyId = intent.getStringExtra("companyId") ?: companyId

        setupRecycler()
        setupSendButton()
        setupTransferButton()

        viewModel.sendMessage(companyId, "Hola, necesito ayuda")

        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSend.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
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
            val text = binding.editTextMessage.text.toString()
            if (text.isBlank()) return@setOnClickListener

            viewModel.sendMessage(companyId, text)
            binding.editTextMessage.text.clear()
        }
    }

    private fun setupTransferButton() {
        binding.buttonTransferHuman.setOnClickListener {
            viewModel.transferToHuman(companyId, currentUserId) { channelId ->
                Toast.makeText(this, "Conversation transferred to human support", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
