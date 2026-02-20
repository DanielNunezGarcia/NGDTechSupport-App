package com.example.ngdtechsupport.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding

class ChatAdapter(
    private var messages: List<ChatMessageModel>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatMessageBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val message = messages[position]
        holder.binding.textViewMessage.text = message.message

        val isMine = message.senderId == currentUserId
        val params = holder.binding.textViewMessage.layoutParams
                as android.widget.FrameLayout.LayoutParams

        val bubbleParams = holder.binding.layoutBubble.layoutParams
                as android.widget.FrameLayout.LayoutParams

        if (isMine) {
            bubbleParams.gravity = android.view.Gravity.END
            holder.binding.textViewMessage.setBackgroundResource(
                com.example.ngdtechsupport.R.drawable.bg_message_mine
            )
        } else {
            bubbleParams.gravity = android.view.Gravity.START
            holder.binding.textViewMessage.setBackgroundResource(
                com.example.ngdtechsupport.R.drawable.bg_message_other
            )
        }

        holder.binding.layoutBubble.layoutParams = bubbleParams

        val timestamp = message.createdAt
        if (timestamp != null) {
            val date = timestamp.toDate()
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            holder.binding.textViewTime.text = format.format(date)
        }
    }

    fun updateData(newMessages: List<ChatMessageModel>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}