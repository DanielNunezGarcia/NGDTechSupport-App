package com.example.ngdtechsupport.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import android.view.View
import android.widget.TextView
import com.example.ngdtechsupport.model.AppModel

class AppAdapter(
    private var apps: List<AppModel>
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    inner class AppViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val nameText: TextView =
            itemView.findViewById(R.id.tvAppName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_dashboard, parent, false)

        return AppViewHolder(view)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(
        holder: AppViewHolder,
        position: Int
    ) {
        holder.nameText.text = apps[position].name
    }

    fun updateApps(newApps: List<AppModel>) {
        apps = newApps
        notifyDataSetChanged()
    }
}