package com.example.ngdtechsupport.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var messages: List<ChatMessageModel> = emptyList()

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
        val isMine = message.senderId == currentUserId

        holder.binding.textViewMessage.text = message.message

        if (!isMine) {
            holder.binding.textViewSender.text = message.senderName
            holder.binding.textViewSender.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.textViewSender.visibility = android.view.View.GONE
        }

        val bubbleParams =
            holder.binding.layoutBubble.layoutParams
                    as android.widget.FrameLayout.LayoutParams

        bubbleParams.gravity =
            if (isMine) android.view.Gravity.END
            else android.view.Gravity.START

        holder.binding.layoutBubble.layoutParams = bubbleParams

        holder.binding.layoutBubble.setBackgroundResource(
            if (isMine) R.drawable.bg_message_mine
            else R.drawable.bg_message_other
        )

        message.timestamp?.let {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.binding.textViewTime.text = format.format(it.toDate())
        }

        if (isMine) {
            holder.binding.textViewStatus.visibility = android.view.View.VISIBLE
            holder.binding.textViewStatus.text =
                if (message.status == "read") "Leído"
                else "Enviado"
        } else {
            holder.binding.textViewStatus.visibility = android.view.View.GONE
        }
    }

    fun updateData(newMessages: List<ChatMessageModel>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}