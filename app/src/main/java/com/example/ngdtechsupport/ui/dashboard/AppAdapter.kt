package com.example.ngdtechsupport.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.model.AppModel

// Adapter del RecyclerView para mostrar la lista de apps
class AppAdapter(
    private var apps: List<AppModel>
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    // ViewHolder: representa una fila (un item) de la lista
    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val tvAppStatus: TextView = itemView.findViewById(R.id.tvAppStatus)
        private val tvLastUpdate: TextView = itemView.findViewById(R.id.tvLastUpdate)

        fun bind(app: AppModel) {
            tvAppName.text = app.name
            tvAppStatus.text = app.status
            tvLastUpdate.text = app.lastUpdate
        }
    }

    // Crea un nuevo ViewHolder cuando el RecyclerView lo necesita
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_app, parent, false)
        return AppViewHolder(view)
    }

    // Devuelve cuántos elementos hay en la lista
    override fun getItemCount(): Int = apps.size

    // Vincula los datos de una posición concreta al ViewHolder
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    // Método auxiliar por si en el futuro quieres actualizar la lista
    fun updateApps(newApps: List<AppModel>) {
        apps = newApps
        notifyDataSetChanged()
    }
}