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

        if (isMine) {
            params.gravity = android.view.Gravity.END
            holder.binding.textViewMessage.setBackgroundResource(
                com.example.ngdtechsupport.R.drawable.bg_message_mine
            )
        } else {
            params.gravity = android.view.Gravity.START
            holder.binding.textViewMessage.setBackgroundResource(
                com.example.ngdtechsupport.R.drawable.bg_message_other
            )
        }

        holder.binding.textViewMessage.layoutParams = params
    }

    fun updateData(newMessages: List<ChatMessageModel>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}