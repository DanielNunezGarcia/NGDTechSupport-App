package com.example.ngdtechsupport.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.example.ngdtechsupport.ui.updates.UpdatesActivity

class AppDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: AppDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_detail)

        val tvBusinessName = findViewById<TextView>(R.id.tvBusinessName)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        val tvSupportType = findViewById<TextView>(R.id.tvSupportType)
        val tvLastUpdate = findViewById<TextView>(R.id.tvLastUpdate)

        val btnChat = findViewById<Button>(R.id.btnChat)
        val btnUpdates = findViewById<Button>(R.id.btnUpdates)

        val companyId = intent.getStringExtra("companyId") ?: return
        val businessId = intent.getStringExtra("businessId") ?: return

        viewModel = ViewModelProvider(this)[AppDetailViewModel::class.java]

        viewModel.business.observe(this) { business ->

            tvBusinessName.text = business.name
            tvStatus.text = business.status

            progressBar.progress = business.progress
            tvProgress.text = "${business.progress}%"

            tvVersion.text = "Versión: ${business.version}"
            tvSupportType.text = "Soporte: ${business.supportType}"
            tvLastUpdate.text = "Última actualización: ${business.lastUpdate}"
        }

        viewModel.loadBusiness(companyId, businessId)

        // Botón Chat
        btnChat.setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)

            startActivity(intent)
        }

        // Botón Updates
        btnUpdates.setOnClickListener {

            val intent = Intent(this, UpdatesActivity::class.java)

            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)
            intent.putExtra("channelId", "private_admin_client")

            startActivity(intent)
        }
    }
}