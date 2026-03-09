package com.example.ngdtechsupport.ui.updates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.model.UpdateModel
import android.widget.TextView
import java.util.Date

class UpdatesAdapter(
    private var updates: List<UpdateModel>
) : RecyclerView.Adapter<UpdatesAdapter.UpdateViewHolder>() {

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

    override fun getItemCount(): Int = updates.size

    override fun onBindViewHolder(holder: UpdateViewHolder, position: Int) {

        val update = updates[position]

        holder.title.text = update.title
        holder.description.text = update.description
        holder.date.text = Date(update.createdAt).toString()
    }

    fun updateList(newUpdates: List<UpdateModel>) {
        updates = newUpdates
        notifyDataSetChanged()
    }
}