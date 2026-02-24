package com.example.ngdtechsupport.ui.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.databinding.ItemChannelBinding

class ChannelAdapter(
    private val currentUserId: String,
    private val onClick: (ChannelModel) -> Unit
) : ListAdapter<ChannelModel, ChannelAdapter.ChannelVH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelVH {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChannelVH(binding)
    }

    override fun onBindViewHolder(holder: ChannelVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChannelVH(
        private val binding: ItemChannelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: ChannelModel) {

            binding.textChannelName.text = channel.name

            val unread = channel.unreadCount[currentUserId] ?: 0

            if (unread > 0) {
                binding.textUnread.visibility = android.view.View.VISIBLE
                binding.textUnread.text = unread.toString()
            } else {
                binding.textUnread.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                onClick(channel)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChannelModel>() {
        override fun areItemsTheSame(
            oldItem: ChannelModel,
            newItem: ChannelModel
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ChannelModel,
            newItem: ChannelModel
        ) = oldItem == newItem
    }
}