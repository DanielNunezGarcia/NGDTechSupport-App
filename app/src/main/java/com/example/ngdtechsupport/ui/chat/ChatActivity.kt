package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
    private val chatViewModel: ChatViewModel by viewModels()

    private var currentUserId: String = ""
    private var companyId: String = ""
    private var businessId: String = ""
    private var channelId: String = ""
    private var replyMessage: ChatMessageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        companyId = intent.getStringExtra("companyId").orEmpty().ifEmpty { "NGDStudios" }
        businessId = intent.getStringExtra("businessId").orEmpty().ifEmpty { "restaurante_madrid" }
        channelId = intent.getStringExtra("channelId").orEmpty().ifEmpty { "${businessId}_support" }

        setupRecycler()
        setupInputActions()
        setupQuickReplies()

        chatViewModel.listenMessages(companyId, channelId)
        chatViewModel.listenTyping(companyId, channelId)
        chatViewModel.updateLastRead(companyId, channelId, currentUserId)
        chatViewModel.markChatAsRead(companyId, channelId, true)

        chatViewModel.messages.observe(this) { messages ->
            adapter.submitMessages(messages) {
                scrollToBottom()
                updateScrollButtons()
            }
        }

        chatViewModel.typingUsers.observe(this) { typingMap ->
            if (typingMap.isEmpty()) {
                binding.textTyping.visibility = View.GONE
            } else {
                val names = typingMap.values.mapNotNull {
                    (it as? Map<*, *>)?.get("name") as? String
                }
                if (names.isNotEmpty()) {
                    binding.textTyping.text = if (names.size == 1) {
                        "${names[0]} esta escribiendo..."
                    } else {
                        "Varias personas estan escribiendo..."
                    }
                    binding.textTyping.visibility = View.VISIBLE
                } else {
                    binding.textTyping.visibility = View.GONE
                }
            }
        }

        chatViewModel.error.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                chatViewModel.clearError()
            }
        }
    }

    private fun setupRecycler() {
        adapter = ChatAdapter(currentUserId) { message ->
            showMessageOptions(message)
        }

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.setHasFixedSize(true)
        binding.recyclerViewChat.adapter = adapter

        binding.recyclerViewChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                updateScrollButtons()
            }
        })

        binding.fabNewMessage.setOnClickListener {
            scrollToBottom()
        }

        binding.fabTopMessage.setOnClickListener {
            binding.recyclerViewChat.smoothScrollToPosition(0)
        }
    }

    private fun updateScrollButtons() {
        val total = layoutManager.itemCount
        if (total <= 1) {
            binding.fabNewMessage.visibility = View.GONE
            binding.fabTopMessage.visibility = View.GONE
            return
        }

        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        binding.fabTopMessage.visibility = if (firstVisible > 2) View.VISIBLE else View.GONE
        binding.fabNewMessage.visibility = if (lastVisible < total - 2) View.VISIBLE else View.GONE
    }

    private fun setupInputActions() {
        binding.buttonSend.setOnClickListener {
            sendCurrentMessage()
        }

        binding.btnClearChat.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Borrar chat")
                .setMessage("Se borraran todos los mensajes de este chat.")
                .setPositiveButton("Borrar") { _, _ ->
                    chatViewModel.clearChat(companyId, channelId)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                chatViewModel.setTyping(companyId, channelId, s?.isNotBlank() == true)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun sendCurrentMessage() {
        val text = binding.editTextMessage.text?.toString().orEmpty().trim()
        if (text.isEmpty()) return

        chatViewModel.sendMessage(
            companyId = companyId,
            channelId = channelId,
            text = text,
            senderId = currentUserId,
            replyToId = replyMessage?.id,
            replyToText = replyMessage?.message
        )

        binding.editTextMessage.text?.clear()
        replyMessage = null
        binding.layoutReplyPreview.visibility = View.GONE
    }

    private fun setupQuickReplies() {
        binding.btnQuick1.setOnClickListener { sendQuickMessage("Quiero consultar el estado de mi proyecto") }
        binding.btnQuick2.setOnClickListener { sendQuickMessage("Tengo un problema en mi aplicacion") }
        binding.btnQuick3.setOnClickListener { sendQuickMessage("Quiero solicitar un presupuesto") }
        binding.btnQuick4.setOnClickListener { sendQuickMessage("Quiero hablar con un agente") }
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

    private fun showMessageOptions(message: ChatMessageModel) {
        val popup = PopupMenu(this, binding.buttonSend)
        popup.menu.add("Responder")
        if (message.senderId == currentUserId) {
            popup.menu.add("Editar mensaje")
            popup.menu.add("Borrar mensaje")
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Responder" -> {
                    replyMessage = message
                    binding.layoutReplyPreview.visibility = View.VISIBLE
                    binding.textReplyingTo.text = "Respondiendo a: ${message.message}"
                    true
                }
                "Editar mensaje" -> {
                    showEditMessageDialog(message)
                    true
                }
                "Borrar mensaje" -> {
                    chatViewModel.deleteMessage(companyId, channelId, message.id)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showEditMessageDialog(message: ChatMessageModel) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setText(message.message)
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar mensaje")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newText = input.text?.toString().orEmpty().trim()
                if (newText.isNotEmpty()) {
                    chatViewModel.editMessage(companyId, channelId, message.id, newText)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun scrollToBottom() {
        if (adapter.itemCount > 0) {
            binding.recyclerViewChat.post {
                binding.recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.setTyping(companyId, channelId, false)
    }
}
