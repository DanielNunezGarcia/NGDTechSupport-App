package com.example.ngdtechsupport.ui.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChatItem
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding
import com.example.ngdtechsupport.databinding.ItemDateSeparatorBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String
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
            MessageVH(binding)

        } else {

            val binding = ItemDateSeparatorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            DateVH(binding)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = items[position]) {

            is ChatItem.MessageItem ->
                (holder as MessageVH).bind(item.message)

            is ChatItem.DateSeparator ->
                (holder as DateVH).binding.textViewDate.text = item.date
        }
    }

    fun submitMessages(messages: List<ChatMessageModel>) {

        val newList = buildListWithSeparators(messages)

        items.clear()
        items.addAll(newList)

        notifyDataSetChanged()
    }

    private fun buildListWithSeparators(
        messages: List<ChatMessageModel>
    ): List<ChatItem> {

        val list = mutableListOf<ChatItem>()
        var lastDate: String? = null

        messages.forEach { message ->

            val dateString = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(message.timestamp?.toDate() ?: Date())

            if (dateString != lastDate) {
                list.add(ChatItem.DateSeparator(dateString))
                lastDate = dateString
            }

            list.add(ChatItem.MessageItem(message))
        }

        return list
    }

    // =============================
    // VIEW HOLDER MENSAJE
    // =============================
    inner class MessageVH(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessageModel) {

            // Texto principal
            binding.textViewMessage.text = message.message

            // 🔥 Mostrar reply si existe
            if (!message.replyToText.isNullOrEmpty()) {

                binding.textViewReply.visibility = View.VISIBLE
                binding.textViewReply.text = "↪ ${message.replyToText}"

            } else {
                binding.textViewReply.visibility = View.GONE
            }

            // Long press destacar
            binding.root.setOnLongClickListener {
                binding.root.setBackgroundColor(
                    android.graphics.Color.parseColor("#FFE082")
                )
                true
            }
        }
    }

    // =============================
    // VIEW HOLDER FECHA
    // =============================
    inner class DateVH(
        val binding: ItemDateSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root)
}