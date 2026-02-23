package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    private lateinit var companyId: String
    private lateinit var businessId: String
    private var currentUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                currentUserName = document.getString("name") ?: "Usuario"
            }

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        setupRecycler()
        observeMessages()
        setupSendButton()

        viewModel.listenMessages(companyId, businessId)
        viewModel.listenTyping(companyId, businessId)

        viewModel.typingUser.observe(this) { userName ->

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (userName != null && userName != currentUserName) {
                binding.textViewTyping.visibility = View.VISIBLE
                binding.textViewTyping.text = "$userName está escribiendo..."
            } else {
                binding.textViewTyping.visibility = View.GONE
            }
        }

        binding.recyclerViewChat.itemAnimator?.apply {
            addDuration = 200
            removeDuration = 200
            moveDuration = 200
            changeDuration = 200
        }

        binding.recyclerViewChat.itemAnimator?.changeDuration = 0

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.markAsRead(companyId, businessId, currentUserId)
    }

    private fun setupRecycler() {

        val currentUserId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid ?: ""

        adapter = ChatAdapter()
        binding.recyclerViewChat.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.adapter = adapter
    }

    private fun observeMessages() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            binding.recyclerViewChat.scrollToPosition(messages.size - 1)

            if (messages.isNotEmpty()) {
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupSendButton() {

        binding.editTextMessage.addTextChangedListener {

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (it.toString().isNotEmpty()) {
                viewModel.setTyping(companyId, businessId, userId, currentUserName, true)
            } else {
                viewModel.setTyping(companyId, businessId, userId, currentUserName, false)
            }
        }

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
                    senderRole,
                    currentUserName
                )

                viewModel.setTyping(companyId, businessId, senderId, currentUserName, false)

                binding.editTextMessage.text.clear()
            }
        }
    }
}