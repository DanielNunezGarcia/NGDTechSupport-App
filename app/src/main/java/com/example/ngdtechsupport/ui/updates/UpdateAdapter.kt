package com.example.ngdtechsupport.ui.updates

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.model.UpdateModel
import java.text.SimpleDateFormat
import java.util.*

class UpdatesAdapter :
    ListAdapter<UpdateModel, UpdatesAdapter.UpdateViewHolder>(DiffCallback()) {

    class UpdateViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_update, parent, false)
        ) {

        val title: TextView = itemView.findViewById(R.id.tvUpdateTitle)
        val description: TextView = itemView.findViewById(R.id.tvUpdateDescription)
        val date: TextView = itemView.findViewById(R.id.tvUpdateDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateViewHolder {
        return UpdateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: UpdateViewHolder, position: Int) {

        val update = getItem(position)

        holder.title.text = update.title
        holder.description.text = update.description

        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.date.text = formatter.format(Date(update.createdAt))
    }

    class DiffCallback : DiffUtil.ItemCallback<UpdateModel>() {

        override fun areItemsTheSame(
            oldItem: UpdateModel,
            newItem: UpdateModel
        ): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: UpdateModel,
            newItem: UpdateModel
        ): Boolean {

            return oldItem == newItem
        }
    }
}