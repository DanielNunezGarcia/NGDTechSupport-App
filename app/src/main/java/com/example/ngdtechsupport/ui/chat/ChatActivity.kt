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
import com.example.ngdtechsupport.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val chatViewModel: ChatViewModel by viewModels()

    private var currentUserId: String = ""
    private var companyId: String = ""
    private var businessId: String = ""
    private var channelId: String = ""
    private var userRole: String = "CLIENT"
    private var replyMessage: ChatMessageModel? = null
    private var pendingAutoScrollToBottom: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Sesion expirada. Inicia sesion otra vez.", Toast.LENGTH_SHORT).show()
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        companyId = intent.getStringExtra("companyId").orEmpty().ifEmpty { DEFAULT_COMPANY_ID }
        businessId = intent.getStringExtra("businessId").orEmpty().ifEmpty { DEFAULT_BUSINESS_ID }
        channelId = intent.getStringExtra("channelId").orEmpty().ifEmpty { "${businessId}_support" }
        userRole = intent.getStringExtra("userRole").orEmpty().ifEmpty { "CLIENT" }.uppercase()

        if (companyId.isBlank() || channelId.isBlank()) {
            Toast.makeText(this, "No se pudo abrir el chat por datos incompletos.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (userRole == "CLIENT") {
            binding.btnClearChat.visibility = View.GONE
        }

        setupRecycler()
        setupInputActions()
        setupQuickReplies()

        chatViewModel.listenMessages(companyId, channelId)
        chatViewModel.listenTyping(companyId, channelId)
        chatViewModel.updateLastRead(companyId, channelId, currentUserId)
        chatViewModel.markChatAsRead(companyId, channelId, userRole == "ADMIN" || userRole == "SOPORTE")

        if (savedInstanceState == null && userRole == "CLIENT") {
            chatViewModel.sendWelcomeMessageOnOpen(companyId, channelId)
        }

        chatViewModel.messages.observe(this) { messages ->
            adapter.submitMessages(messages) {
                if (pendingAutoScrollToBottom) {
                    scrollToBottom()
                    pendingAutoScrollToBottom = false
                }
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
        adapter = ChatAdapter(currentUserId) { anchor, message ->
            showMessageOptions(anchor, message)
        }

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.setHasFixedSize(true)
        binding.recyclerViewChat.setItemViewCacheSize(20)
        binding.recyclerViewChat.setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(1, 30)
            setMaxRecycledViews(2, 10)
        })
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

        binding.recyclerViewChat.post { updateScrollButtons() }
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

        binding.fabTopMessage.visibility = if (firstVisible > 0) View.VISIBLE else View.GONE
        binding.fabNewMessage.visibility = if (lastVisible in 0 until (total - 1)) View.VISIBLE else View.GONE
    }

    private fun setupInputActions() {
        binding.buttonSend.setOnClickListener {
            sendCurrentMessage()
        }

        binding.btnClearChat.setOnClickListener {
            if (companyId.isBlank() || channelId.isBlank()) {
                Toast.makeText(this, "No se pudo borrar: chat invalido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "No se pudo enviar: sesion no valida.", Toast.LENGTH_SHORT).show()
            return
        }

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
        pendingAutoScrollToBottom = true
        scrollToBottom()
    }

    private fun setupQuickReplies() {
        binding.btnQuick1.setOnClickListener { sendQuickMessage("Quiero consultar el estado de mi proyecto") }
        binding.btnQuick2.setOnClickListener { sendQuickMessage("Tengo un problema en mi aplicacion") }
        binding.btnQuick3.setOnClickListener { sendQuickMessage("Quiero solicitar un presupuesto") }
        binding.btnQuick4.setOnClickListener { sendQuickMessage("Quiero hablar con un agente") }
    }

    private fun sendQuickMessage(text: String) {
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "No se pudo enviar: sesion no valida.", Toast.LENGTH_SHORT).show()
            return
        }

        chatViewModel.sendMessage(
            companyId = companyId,
            channelId = channelId,
            text = text,
            senderId = currentUserId,
            replyToId = null,
            replyToText = null
        )
        pendingAutoScrollToBottom = true
        scrollToBottom()
    }

    private fun showMessageOptions(anchorView: View, message: ChatMessageModel) {
        if (message.id.isBlank()) {
            Toast.makeText(this, "El mensaje aun no esta disponible.", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            val popup = PopupMenu(this, anchorView)
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
                        showDeleteMessageDialog(message)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }.onFailure {
            Toast.makeText(this, "No se pudieron abrir las acciones del mensaje.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteMessageDialog(message: ChatMessageModel) {
        AlertDialog.Builder(this)
            .setTitle("Borrar mensaje")
            .setMessage("Este mensaje se eliminara de forma permanente.")
            .setPositiveButton("Borrar") { _, _ ->
                if (message.id.isBlank()) {
                    Toast.makeText(this, "No se pudo borrar el mensaje.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                chatViewModel.deleteMessage(companyId, channelId, message.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditMessageDialog(message: ChatMessageModel) {
        if (message.id.isBlank()) {
            Toast.makeText(this, "No se pudo editar el mensaje.", Toast.LENGTH_SHORT).show()
            return
        }

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
                    pendingAutoScrollToBottom = true
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
