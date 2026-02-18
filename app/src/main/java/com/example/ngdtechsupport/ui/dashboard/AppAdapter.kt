package com.example.ngdtechsupport.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
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
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val tvVersion: TextView = itemView.findViewById(R.id.tvVersion)
        private val tvSupportType: TextView = itemView.findViewById(R.id.tvSupportType)
        private val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)

        fun bind(app: AppModel) {

            val context = itemView.context

            tvAppName.text = app.name
            tvAppStatus.text = app.status
            tvLastUpdate.text = "Última actualización: ${app.lastUpdate}"

            val safeProgress = app.progress.coerceIn(0, 100)
            progressBar.progress = safeProgress
            tvProgress.text = "$safeProgress%"

            tvVersion.text = "Versión: ${app.version}"
            tvSupportType.text = "Soporte: ${app.supportType}"

            val colorRes = when (app.status.lowercase()) {
                "en desarrollo" -> R.color.status_development
                "en revisión" -> R.color.status_review
                "en produccion", "en producción" -> R.color.status_production
                "en mantenimiento" -> R.color.status_maintenance
                "incidencia" -> R.color.status_issue
                else -> R.color.status_default
            }

            tvAppStatus.setTextColor(ContextCompat.getColor(context, colorRes))

            progressBar.progressTintList =
                ContextCompat.getColorStateList(context, colorRes)

            tvAppStatus.background.setTint(
                ContextCompat.getColor(context, colorRes)
            )
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