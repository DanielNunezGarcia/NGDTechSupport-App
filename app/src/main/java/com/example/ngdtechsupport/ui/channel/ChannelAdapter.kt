package com.example.ngdtechsupport.ui.channel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.databinding.ItemChannelBinding

class ChannelAdapter(
    private val viewModel: ChannelViewModel,
    private val companyId: String,
    private val currentUserId: String = ""
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var channels: List<ChannelModel> = emptyList()

    inner class ChannelViewHolder(
        private val binding: ItemChannelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: ChannelModel) {

            binding.textChannelName.text = channel.name

            val isMuted = channel.mutedUsers?.get(currentUserId) == true
            binding.imageMuted.visibility = if (isMuted) View.VISIBLE else View.GONE

            binding.imagePinned.visibility = if (channel.pinned == true) View.VISIBLE else View.GONE

            val unread = channel.unreadCount?.values?.sum() ?: 0

            if (unread > 0) {
                binding.textUnread.visibility = View.VISIBLE
                binding.textUnread.text = unread.toString()
            } else {
                binding.textUnread.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                viewModel.onChannelClick(channel)
            }

            binding.root.setOnLongClickListener { view ->
                showPopupMenu(view, channel)
                true
            }
        }

        private fun showPopupMenu(view: View, channel: ChannelModel) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_channel_options, popup.menu)

            val isMuted = channel.mutedUsers?.get(currentUserId) == true
            val isPinned = channel.pinned == true
            val isArchived = channel.isArchived == true

            popup.menu.findItem(R.id.action_mute)?.title = if (isMuted) "Activar sonido" else "Silenciar"
            popup.menu.findItem(R.id.action_pin)?.title = if (isPinned) "Desfijar" else "Fijar"
            popup.menu.findItem(R.id.action_archive)?.title = if (isArchived) "Desarchivar" else "Archivar"

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_mute -> {
                        viewModel.toggleMute(companyId, channel.id, currentUserId, !isMuted)
                        true
                    }
                    R.id.action_pin -> {
                        viewModel.togglePin(companyId, channel.id, !isPinned)
                        true
                    }
                    R.id.action_archive -> {
                        viewModel.archiveChannel(companyId, channel.id, !isArchived)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
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

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount(): Int = channels.size

    fun submitList(newChannels: List<ChannelModel>) {
        channels = newChannels
        notifyDataSetChanged()
    }
}
