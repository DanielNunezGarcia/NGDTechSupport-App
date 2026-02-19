package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var adapter: ChatAdapter

    private lateinit var companyId: String
    private lateinit var businessId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        setupRecycler()
        observeMessages()
        setupSendButton()

        viewModel.loadMessages(companyId, businessId)
    }

    private fun setupRecycler() {

        val currentUserId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid ?: ""

        adapter = ChatAdapter(emptyList(), currentUserId)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.adapter = adapter
    }

    private fun observeMessages() {
        viewModel.messages.observe(this) { messages ->
            adapter.updateData(messages)

            if (messages.isNotEmpty()) {
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupSendButton() {

        binding.buttonSend.setOnClickListener {

            val text = binding.editTextMessage.text.toString().trim()

            if (text.isNotEmpty()) {

                val senderId = FirebaseAuth.getInstance()
                    .currentUser
                    ?.uid ?: ""

                val senderRole = "Admin" // luego lo haremos dinámico

                viewModel.sendMessage(
                    companyId,
                    businessId,
                    text,
                    senderId,
                    senderRole
                )

                binding.editTextMessage.text.clear()
            }
        }
    }
}