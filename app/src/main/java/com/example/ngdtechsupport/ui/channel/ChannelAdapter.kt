package com.example.ngdtechsupport.ui.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.databinding.ItemChannelBinding

class ChannelAdapter(
    private val viewModel: ChannelViewModel,
    private val companyId: String
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var channels: List<ChannelModel> = emptyList()

    inner class ChannelViewHolder(
        private val binding: ItemChannelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: ChannelModel) {

            // 👇 ESTE ES EL ID REAL DE TU XML
            binding.textChannelName.text = channel.name

            // Mostrar unread si existe
            val unread = channel.unreadCount.values.sum()

            if (unread > 0) {
                binding.textUnread.visibility = android.view.View.VISIBLE
                binding.textUnread.text = unread.toString()
            } else {
                binding.textUnread.visibility = android.view.View.GONE
            }

            // Click largo para archivar
            binding.root.setOnLongClickListener {
                viewModel.archiveChannel(
                    companyId = companyId,
                    channelId = channel.id,
                    archived = true
                )
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChannelViewHolder(binding)
    }

    override fun getItemCount(): Int = channels.size

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    fun submitList(list: List<ChannelModel>) {
        channels = list
        notifyDataSetChanged()
    }
}