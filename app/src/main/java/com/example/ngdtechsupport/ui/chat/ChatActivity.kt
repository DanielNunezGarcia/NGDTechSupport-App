package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.repository.ChatRepository
import com.example.ngdtechsupport.databinding.ActivityChatBinding
import com.example.ngdtechsupport.ui.channel.ChannelViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var currentUserId: String = ""
    private lateinit var companyId: String
    private lateinit var businessId: String
    private val chatViewModel: ChatViewModel by viewModels()

    private var replyMessage: ChatMessageModel? = null
    private var isUserAtBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        setupRecycler()
        setupSendButton()
    }

    private fun setupRecycler() {

        adapter = ChatAdapter(currentUserId) { message ->
            replyMessage = message
            showReplyPreview(message)
        }

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.adapter = adapter

        binding.recyclerViewChat.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                isUserAtBottom = lastVisible >= total - 2

                if (isUserAtBottom) {
                    hideNewMessageIndicator()
                }
            }
        })

        binding.fabNewMessage.setOnClickListener {
            scrollToBottom()
        }
    }

    private fun setupSendButton() {

        binding.buttonSend.setOnClickListener {

            val text = binding.editTextMessage.text.toString()
            if (text.isBlank()) return@setOnClickListener

            chatViewModel.sendMessage(
                companyId = companyId,
                businessId = businessId,
                text = text,
                senderId = currentUserId,
                replyToId = replyMessage?.id,
                replyToText = replyMessage?.message
            )

            binding.editTextMessage.text.clear()
            replyMessage = null
            binding.layoutReplyPreview.visibility = View.GONE
        }
    }

    private fun showReplyPreview(message: ChatMessageModel) {
        binding.layoutReplyPreview.visibility = View.VISIBLE
        binding.textReplyingTo.text = "Respondiendo a: ${message.message}"
    }

    private fun scrollToBottom() {
        binding.recyclerViewChat.post {
            binding.recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
        }
        hideNewMessageIndicator()
    }

    private fun hideNewMessageIndicator() {
        binding.fabNewMessage.visibility = View.GONE
    }
}