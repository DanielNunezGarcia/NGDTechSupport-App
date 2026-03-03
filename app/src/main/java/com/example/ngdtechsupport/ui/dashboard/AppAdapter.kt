package com.example.ngdtechsupport.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import android.widget.TextView
import com.example.ngdtechsupport.model.AppModel

class AppAdapter(
    private var apps: List<AppModel>,
    private val onAppClick: (AppModel) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    inner class AppViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val nameText: TextView = itemView.findViewById(R.id.tvAppName)
        private val statusText: TextView = itemView.findViewById(R.id.tvAppStatus)
        private val statusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val progressText: TextView = itemView.findViewById(R.id.tvProgress)
        private val versionText: TextView = itemView.findViewById(R.id.tvVersion)
        private val supportTypeText: TextView = itemView.findViewById(R.id.tvSupportType)
        private val lastUpdateText: TextView = itemView.findViewById(R.id.tvLastUpdate)

        fun bind(app: AppModel) {
            nameText.text = app.name
            statusText.text = app.status

            progressBar.progress = app.progress.coerceIn(0, 100)
            progressText.text = "${app.progress.coerceIn(0, 100)}%"

            versionText.text = if (app.version.isNotBlank()) {
                "Versión: ${app.version}"
            } else {
                "Versión: -"
            }

            supportTypeText.text = if (app.supportType.isNotBlank()) {
                "Soporte: ${app.supportType}"
            } else {
                "Soporte: -"
            }

            lastUpdateText.text = if (app.lastUpdate.isNotBlank()) {
                "Última actualización: ${app.lastUpdate}"
            } else {
                "Última actualización: -"
            }

            // Icono simple según estado (ajusta si quieres)
            val iconRes = when (app.status.lowercase()) {
                "en desarrollo" -> android.R.drawable.presence_away
                "completado", "finalizado" -> android.R.drawable.presence_online
                "pausado" -> android.R.drawable.presence_busy
                else -> android.R.drawable.presence_invisible
            }
            statusIcon.setImageResource(iconRes)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_app, parent, false)

        return AppViewHolder(view)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(
        holder: AppViewHolder,
        position: Int
    ) {
        val app = apps[position]
        holder.bind(app)

        holder.itemView.setOnClickListener {
            onAppClick(app)
        }
    }

    fun updateApps(newApps: List<AppModel>) {
        apps = newApps
        notifyDataSetChanged()
    }
}