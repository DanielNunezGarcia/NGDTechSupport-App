package com.example.ngdtechsupport.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.example.ngdtechsupport.ui.updates.UpdatesActivity

class AppDetailActivity : AppCompatActivity() {

    private val viewModel: AppDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        val businessId = intent.getStringExtra("businessId") ?: return
        val companyId = intent.getStringExtra("companyId") ?: return

        val name = findViewById<TextView>(R.id.tvBusinessName)
        val status = findViewById<TextView>(R.id.tvStatus)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progress = findViewById<TextView>(R.id.tvProgress)
        val version = findViewById<TextView>(R.id.tvVersion)
        val support = findViewById<TextView>(R.id.tvSupportType)
        val lastUpdate = findViewById<TextView>(R.id.tvLastUpdate)

        val chatButton = findViewById<Button>(R.id.btnChat)
        val updatesButton = findViewById<Button>(R.id.btnUpdates)

        viewModel.uiState.observe(this) { state ->

            val business = state.business ?: return@observe

            name.text = business.name
            status.text = business.status

            progressBar.progress = business.progress
            progress.text = "${business.progress}%"

            version.text = "Versión: ${business.version}"
            support.text = "Soporte: ${business.supportType}"
            lastUpdate.text = "Última actualización: ${business.lastUpdate}"
        }

        viewModel.loadBusiness(companyId, businessId)

        chatButton.setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)

            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)

            startActivity(intent)
        }

        updatesButton.setOnClickListener {

            val intent = Intent(this, UpdatesActivity::class.java)

            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)

            startActivity(intent)
        }
    }
}