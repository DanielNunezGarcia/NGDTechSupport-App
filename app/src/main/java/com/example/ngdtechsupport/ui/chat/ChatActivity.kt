package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
    private lateinit var companyId: String
    private lateinit var businessId: String
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var channelId: String

    private var replyMessage: ChatMessageModel? = null
    private var isUserAtBottom = true
    
    // Typing indicator
    private val typingHandler = Handler(Looper.getMainLooper())
    private var typingRunnable: Runnable? = null
    private val typingDelay = 2000L // 2 segundos de inactividad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""
        channelId = intent.getStringExtra("channelId") ?: ""

        setupRecycler()
        setupSendButton()
        setupTypingIndicator()
        setupQuickReplies()

        // Marcar como leído mensaje en el Chat
        chatViewModel.markChatAsRead(
            companyId,
            channelId,
            true
        )

        chatViewModel.listenMessages(companyId, businessId)
        
        // Escuchar typing
        chatViewModel.listenTyping(companyId, channelId)

        chatViewModel.messages.observe(this) { messages ->
            adapter.submitMessages(messages)

            if (messages.isNotEmpty()) {
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }

        chatViewModel.typingUsers.observe(this) { typingMap ->
            updateTypingIndicator(typingMap)
        }

        chatViewModel.updateLastRead(
            companyId,
            businessId,
            currentUserId
        )
    }

    private fun setupTypingIndicator() {
        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Usuario está escribiendo
                chatViewModel.setTyping(companyId, channelId, true)
                
                // Cancelar el typing anterior
                typingRunnable?.let { typingHandler.removeCallbacks(it) }
                
                // Establecer nuevo timer para dejar de mostrar "escribiendo"
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

    // Función Botón Enviar
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

            // Dejar de mostrar "escribiendo"
            chatViewModel.setTyping(companyId, channelId, false)
            typingRunnable?.let { typingHandler.removeCallbacks(it) }

            binding.editTextMessage.text.clear()
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

    override fun onDestroy() {
        super.onDestroy()
        typingRunnable?.let { typingHandler.removeCallbacks(it) }
        chatViewModel.setTyping(companyId, channelId, false)
    }
}