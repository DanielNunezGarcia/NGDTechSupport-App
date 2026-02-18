package com.example.ngdtechsupport.ui.updates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.model.UpdateModel
import java.text.SimpleDateFormat
import java.util.*

class UpdateAdapter(
    private var updates: List<UpdateModel>
) : RecyclerView.Adapter<UpdateAdapter.UpdateViewHolder>() {

    class UpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvUpdateTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvUpdateDescription)
        val tvDate: TextView = itemView.findViewById(R.id.tvUpdateDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_update, parent, false)
        return UpdateViewHolder(view)
    }

    override fun getItemCount(): Int = updates.size

    override fun onBindViewHolder(holder: UpdateViewHolder, position: Int) {
        val update = updates[position]

        holder.tvTitle.text = update.title
        holder.tvDescription.text = update.description

        val date = update.createdAt?.toDate()
        if (date != null) {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.tvDate.text = format.format(date)
        } else {
            holder.tvDate.text = ""
        }
    }

    fun updateList(newUpdates: List<UpdateModel>) {
        updates = newUpdates
        notifyDataSetChanged()
    }
}