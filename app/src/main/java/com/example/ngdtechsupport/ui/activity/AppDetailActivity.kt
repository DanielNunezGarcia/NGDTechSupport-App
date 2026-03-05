package com.example.ngdtechsupport.ui.activity

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R

class AppDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        val businessId = intent.getStringExtra("businessId") ?: return
        val companyId = intent.getStringExtra("companyId") ?: return

        val btnChat = findViewById<Button>(R.id.btnChatDetail)
        val btnUpdates = findViewById<Button>(R.id.btnUpdatesDetail)

        btnChat.setOnClickListener {

            val intent = Intent(
                this,
                com.example.ngdtechsupport.ui.chat.ChatActivity::class.java
            )

            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)

            startActivity(intent)
        }

        btnUpdates.setOnClickListener {

            val intent = Intent(
                this,
                com.example.ngdtechsupport.ui.updates.UpdatesActivity::class.java
            )

            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)

            startActivity(intent)
        }
    }
}