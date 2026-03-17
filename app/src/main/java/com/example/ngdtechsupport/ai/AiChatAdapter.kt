package com.example.ngdtechsupport.ai

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.databinding.ItemAiMessageBinding

class AiChatAdapter(
    private val currentUserId: String
) : ListAdapter<AiMessage, AiChatAdapter.MessageViewHolder>(AiMessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemAiMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: ItemAiMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: AiMessage) {
            binding.textMessage.text = message.content

            val isUser = message.role == AiRole.USER

            val layoutParams = binding.layoutBubble.layoutParams as ViewGroup.MarginLayoutParams
            if (isUser) {
                binding.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_sent)
                binding.layoutBubble.gravity = Gravity.END
                layoutParams.marginStart = 100
                layoutParams.marginEnd = 8
            } else {
                binding.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_received)
                binding.layoutBubble.gravity = Gravity.START
                layoutParams.marginStart = 8
                layoutParams.marginEnd = 100
            }
            binding.layoutBubble.layoutParams = layoutParams
        }
    }

    class AiMessageDiffCallback : DiffUtil.ItemCallback<AiMessage>() {
        override fun areItemsTheSame(oldItem: AiMessage, newItem: AiMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AiMessage, newItem: AiMessage): Boolean {
            return oldItem == newItem
        }
    }
}
