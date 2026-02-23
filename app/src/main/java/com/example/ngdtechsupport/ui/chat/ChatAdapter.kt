package com.example.ngdtechsupport.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatMessageModel>()

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
            holder.binding.textViewSender.visibility = View.VISIBLE
            holder.binding.textViewSender.text = message.senderName
        } else {
            holder.binding.textViewSender.visibility = View.GONE
        }

        val bubbleParams =
            holder.binding.layoutBubble.layoutParams as android.widget.FrameLayout.LayoutParams

        bubbleParams.gravity =
            if (isMine) android.view.Gravity.END
            else android.view.Gravity.START

        holder.binding.layoutBubble.layoutParams = bubbleParams

        holder.binding.layoutBubble.setBackgroundResource(
            if (isMine) R.drawable.bg_message_mine
            else R.drawable.bg_message_other
        )

        holder.binding.textViewTime.text = formatTimestamp(message.timestamp)

        if (isMine) {
            holder.binding.textViewStatus.visibility = View.VISIBLE
            holder.binding.textViewStatus.text =
                if (message.status == "read") "✓✓"
                else "✓"
        } else {
            holder.binding.textViewStatus.visibility = View.GONE
        }
    }

    // Formato hora profesional
    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""

        val date = timestamp.toDate()
        val now = Calendar.getInstance()
        val msg = Calendar.getInstance().apply { time = date }

        val sameDay =
            now.get(Calendar.YEAR) == msg.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val fullFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

        return if (sameDay) timeFormat.format(date)
        else fullFormat.format(date)
    }

    fun submitMessages(newList: List<ChatMessageModel>) {

        val diffCallback = object : DiffUtil.Callback() {

            override fun getOldListSize() = messages.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                messages[oldPos].id == newList[newPos].id

            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                messages[oldPos] == newList[newPos]
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages.clear()
        messages.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }
}