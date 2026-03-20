package com.example.ngdtechsupport.ui.updates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.model.UpdateModel
import java.text.SimpleDateFormat
import java.util.*

class UpdatesAdapter(
    private val onItemClick: (UpdateModel) -> Unit = {}
) : ListAdapter<UpdateModel, UpdatesAdapter.UpdateViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_update, parent, false)
        return UpdateViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpdateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvUpdateTitle)
        private val description: TextView = itemView.findViewById(R.id.tvUpdateDescription)
        private val date: TextView = itemView.findViewById(R.id.tvUpdateDate)
        private val type: TextView = itemView.findViewById(R.id.tvUpdateType)

        fun bind(update: UpdateModel) {
            title.text = update.title
            description.text = update.description

            val formatter = SimpleDateFormat("dd MMM yyyy 'a las' HH:mm", Locale.getDefault())
            date.text = "Actualizado: ${formatter.format(Date(update.createdAt))}"

            if (update.type.isNotEmpty()) {
                type.visibility = View.VISIBLE
                type.text = update.type
                type.setBackgroundResource(getTypeBackground(update.type))
            } else {
                type.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(update)
            }
        }

        private fun getTypeBackground(type: String): Int {
            return when (type.lowercase()) {
                "nuevo" -> R.drawable.bg_badge_new
                "actualizacion" -> R.drawable.bg_badge_update
                "importante" -> R.drawable.bg_badge_important
                else -> R.drawable.bg_badge
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UpdateModel>() {
        override fun areItemsTheSame(oldItem: UpdateModel, newItem: UpdateModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UpdateModel, newItem: UpdateModel): Boolean {
            return oldItem == newItem
        }
    }
}
