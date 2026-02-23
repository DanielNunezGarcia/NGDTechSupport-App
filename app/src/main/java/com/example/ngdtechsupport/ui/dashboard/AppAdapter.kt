package com.example.ngdtechsupport.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding

class ChatAdapter :
    ListAdapter<ChatMessageModel, ChatAdapter.ChatViewHolder>(DiffCallback()) {

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

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = getItem(position)
        holder.binding.textViewMessage.text = message.message
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessageModel>() {

        override fun areItemsTheSame(
            oldItem: ChatMessageModel,
            newItem: ChatMessageModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ChatMessageModel,
            newItem: ChatMessageModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}