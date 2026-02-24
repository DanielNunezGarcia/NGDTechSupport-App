package com.example.ngdtechsupport.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.*
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding
import com.example.ngdtechsupport.databinding.ItemDateSeparatorBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String,
    private val onLongClick: (ChatMessageModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ChatItem>()

    companion object {
        private const val TYPE_MESSAGE = 1
        private const val TYPE_DATE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
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

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = items[position]) {

            is ChatItem.MessageItem -> {
                (holder as MessageViewHolder).bind(item.message)
            }

            is ChatItem.DateSeparator -> {
                (holder as DateViewHolder).binding.textViewDate.text = item.date
            }
        }
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

        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessageModel) {

            binding.textViewMessage.text = message.message

            // 🔥 Mostrar preview de respuesta dentro de la burbuja
            if (!message.replyToText.isNullOrEmpty()) {
                binding.textViewReplyPreview.visibility = android.view.View.VISIBLE
                binding.textViewReplyPreview.text = message.replyToText
            } else {
                binding.textViewReplyPreview.visibility = android.view.View.GONE
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