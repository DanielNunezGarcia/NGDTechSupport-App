package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var companyId: String
    private lateinit var businessId: String
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerViewChat)

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        setupRecycler()
        observeMessages()

        viewModel.loadMessages(companyId, businessId)

        // Enviar mensaje. Botón y texto
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        setupSendButton()
    }

    private fun setupSendButton() {
        buttonSend.setOnClickListener {
            val text = editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {

                val senderId = com.google.firebase.auth.FirebaseAuth
                    .getInstance()
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

                editTextMessage.text.clear()
            }
        }
    }

    private fun setupRecycler() {
        adapter = ChatAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeMessages() {
        viewModel.messages.observe(this) { messages ->
            adapter.updateData(messages)
        }
    }
}