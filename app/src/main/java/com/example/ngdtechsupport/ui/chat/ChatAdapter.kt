package com.example.ngdtechsupport.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding
import com.example.ngdtechsupport.databinding.ItemDateSeparatorBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String,
    private val onLongClick: (ChatMessageModel) -> Unit
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val TYPE_MESSAGE = 1
        private const val TYPE_DATE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatItem.MessageItem -> TYPE_MESSAGE
            is ChatItem.DateSeparator -> TYPE_DATE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : RecyclerView.ViewHolder {

        return if (viewType == TYPE_MESSAGE) {

            val binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MessageViewHolder(binding)

        } else {

            val binding = ItemDateSeparatorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            DateViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = getItem(position)) {

            is ChatItem.MessageItem -> {
                (holder as MessageViewHolder).bind(item.message)
            }

            is ChatItem.DateSeparator -> {
                (holder as DateViewHolder).binding.textViewDate.text = item.date
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    fun submitMessages(messages: List<ChatMessageModel>) {
        val newItems = mutableListOf<ChatItem>()
        var lastDate: String? = null

        messages.forEach { message ->
            val dateString = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(message.timestamp?.toDate() ?: Date())

            if (dateString != lastDate) {
                newItems.add(ChatItem.DateSeparator(dateString))
                lastDate = dateString
            }

            newItems.add(ChatItem.MessageItem(message))
        }

        submitList(newItems)
    }

    inner class MessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessageModel) {
            val isOwnMessage = message.senderId == currentUserId
            val isAiMessage = message.senderType == ChatMessageModel.SENDER_TYPE_AI
            val isAgentMessage = message.senderType == ChatMessageModel.SENDER_TYPE_AGENT
            val context = binding.root.context

            // Mensaje texto
            binding.textViewMessage.text = message.message

            // Diferenciación visual según tipo de remitente
            val layoutParams = binding.layoutBubble.layoutParams as ViewGroup.MarginLayoutParams
            
            when {
                // Mensaje del usuario actual
                isOwnMessage -> {
                    binding.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_sent)
                    binding.layoutBubble.gravity = Gravity.END
                    layoutParams.marginStart = 100
                    layoutParams.marginEnd = 8
                }
                // Mensaje de IA
                isAiMessage -> {
                    binding.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_ai)
                    binding.layoutBubble.gravity = Gravity.START
                    layoutParams.marginStart = 8
                    layoutParams.marginEnd = 100
                }
                // Mensaje de agente humano
                isAgentMessage -> {
                    binding.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_agent)
                    binding.layoutBubble.gravity = Gravity.START
                    layoutParams.marginStart = 8
                    layoutParams.marginEnd = 100
                }
                // Otro usuario
                else -> {
                    binding.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_received)
                    binding.layoutBubble.gravity = Gravity.START
                    layoutParams.marginStart = 8
                    layoutParams.marginEnd = 100
                }
            }
            binding.layoutBubble.layoutParams = layoutParams

            // Nombre del remitente según tipo
            when {
                isAiMessage -> {
                    binding.textViewSender.visibility = View.VISIBLE
                    binding.textViewSender.text = "🤖 Asistente IA"
                }
                isAgentMessage -> {
                    binding.textViewSender.visibility = View.VISIBLE
                    binding.textViewSender.text = "👨‍💼 ${message.senderName}"
                }
                !isOwnMessage && !message.senderName.isNullOrEmpty() -> {
                    binding.textViewSender.visibility = View.VISIBLE
                    binding.textViewSender.text = message.senderName
                }
                else -> {
                    binding.textViewSender.visibility = View.GONE
                }
            }

            // Preview de respuesta
            if (!message.replyToText.isNullOrEmpty()) {
                binding.textViewReplyPreview.visibility = View.VISIBLE
                binding.textViewReplyPreview.text = message.replyToText
                binding.textViewReplyUser.text = message.replyToUserName ?: ""
            } else {
                binding.textViewReplyPreview.visibility = View.GONE
            }

            // Hora del mensaje
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = timeFormat.format(message.timestamp?.toDate() ?: Date())
            binding.textViewTime.text = timeString
            binding.textViewTime.visibility = View.VISIBLE

            // Estado del mensaje (solo para mensajes propios)
            if (isOwnMessage) {
                binding.textViewStatus.visibility = View.VISIBLE
                binding.textViewStatus.text = when (message.status) {
                    "sent" -> "✓"
                    "delivered" -> "✓✓"
                    "read" -> "✓✓"
                    else -> "✓"
                }
                binding.textViewStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.status_read)
                )
            } else {
                binding.textViewStatus.visibility = View.GONE
            }

            binding.root.setOnLongClickListener {
                onLongClick(message)
                true
            }
        }
    }

    inner class DateViewHolder(
        val binding: ItemDateSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root)
}

class ChatDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return when {
            oldItem is ChatItem.MessageItem && newItem is ChatItem.MessageItem ->
                oldItem.message.id == newItem.message.id
            oldItem is ChatItem.DateSeparator && newItem is ChatItem.DateSeparator ->
                oldItem.date == newItem.date
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem == newItem
    }
}

sealed class ChatItem {
    data class MessageItem(val message: ChatMessageModel) : ChatItem()
    data class DateSeparator(val date: String) : ChatItem()
}
