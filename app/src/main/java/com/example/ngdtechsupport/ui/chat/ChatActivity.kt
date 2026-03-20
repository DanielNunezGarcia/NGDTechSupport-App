package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var currentUserId: String = ""
    private var companyId: String = ""
    private var businessId: String = ""
    private var channelId: String = ""
    private val chatViewModel: ChatViewModel by viewModels()

    private var replyMessage: ChatMessageModel? = null
    
    private val typingHandler = Handler(Looper.getMainLooper())
    private var typingRunnable: Runnable? = null
    private val typingDelay = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""
        channelId = intent.getStringExtra("channelId") ?: getDefaultChannelId()

        if (companyId.isEmpty() || channelId.isEmpty()) {
            Toast.makeText(this, "Error: Datos de chat no disponibles", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecycler()
        setupSendButton()
        setupTypingIndicator()
        setupQuickReplies()

        chatViewModel.markChatAsRead(companyId, channelId, true)

        chatViewModel.listenMessages(companyId, channelId)
        
        chatViewModel.listenTyping(companyId, channelId)

        chatViewModel.messages.observe(this) { messages ->
            val itemCount = adapter.itemCount
            adapter.submitMessages(messages) {
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.post {
                        binding.recyclerViewChat.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        chatViewModel.typingUsers.observe(this) { typingMap ->
            updateTypingIndicator(typingMap)
        }

        chatViewModel.updateLastRead(companyId, channelId, currentUserId)
    }

    private fun getDefaultChannelId(): String {
        return if (businessId.isNotEmpty()) {
            "${businessId}_support"
        } else {
            "default_support"
        }
    }

    private fun setupTypingIndicator() {
        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                chatViewModel.setTyping(companyId, channelId, true)
                
                typingRunnable?.let { typingHandler.removeCallbacks(it) }
                
                typingRunnable = Runnable {
                    chatViewModel.setTyping(companyId, channelId, false)
                }
                typingHandler.postDelayed(typingRunnable!!, typingDelay)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateTypingIndicator(typingMap: Map<String, Any>) {
        if (typingMap.isEmpty()) {
            binding.textTyping.visibility = View.GONE
            return
        }
        
        val names = typingMap.values.mapNotNull {
            (it as? Map<*, *>)?.get("name") as? String
        }
        
        val text = when {
            names.size == 1 -> "${names[0]} está escribiendo..."
            names.size == 2 -> "${names[0]} y ${names[1]} están escribiendo..."
            names.size > 2 -> "Varias personas están escribiendo..."
            else -> ""
        }
        
        if (text.isNotEmpty()) {
            binding.textTyping.visibility = View.VISIBLE
            binding.textTyping.text = text
        } else {
            binding.textTyping.visibility = View.GONE
        }
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
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (lastVisible >= total - 2) {
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
                channelId = channelId,
                text = text,
                senderId = currentUserId,
                replyToId = replyMessage?.id,
                replyToText = replyMessage?.message
            )

            chatViewModel.setTyping(companyId, channelId, false)
            typingRunnable?.let { typingHandler.removeCallbacks(it) }

            binding.editTextMessage.text?.clear()
            replyMessage = null
            binding.layoutReplyPreview.visibility = View.GONE
        }
    }

    private fun showReplyPreview(message: ChatMessageModel) {
        binding.layoutReplyPreview.visibility = View.VISIBLE
        binding.textReplyingTo.text = "Respondiendo a: ${message.message}"
    }

    private fun setupQuickReplies() {
        binding.btnQuick1.setOnClickListener {
            sendQuickMessage("Quiero consultar el estado de mi proyecto")
        }
        
        binding.btnQuick2.setOnClickListener {
            sendQuickMessage("Tengo un problema/error en mi aplicación")
        }
        
        binding.btnQuick3.setOnClickListener {
            sendQuickMessage("Quiero solicitar un presupuesto")
        }
        
        binding.btnQuick4.setOnClickListener {
            sendQuickMessage("Quiero hablar con un agente humano")
        }
    }

    private fun sendQuickMessage(text: String) {
        chatViewModel.sendMessage(
            companyId = companyId,
            channelId = channelId,
            text = text,
            senderId = currentUserId,
            replyToId = null,
            replyToText = null
        )
        
        binding.recyclerViewChat.postDelayed({
            binding.recyclerViewChat.smoothScrollToPosition(adapter.itemCount)
        }, 300)
    }

    private fun scrollToBottom() {
        binding.recyclerViewChat.post {
            binding.recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
        }
        hideNewMessageIndicator()
    }

    private fun hideNewMessageIndicator() {
        binding.fabNewMessage.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        typingRunnable?.let { typingHandler.removeCallbacks(it) }
        chatViewModel.setTyping(companyId, channelId, false)
    }
}
