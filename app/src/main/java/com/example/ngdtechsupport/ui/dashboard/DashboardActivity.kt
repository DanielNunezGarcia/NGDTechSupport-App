package com.example.ngdtechsupport.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.example.ngdtechsupport.ui.updates.UpdatesActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnChat = findViewById<Button>(R.id.btnChat)
        val btnUpdates = findViewById<Button>(R.id.btnUpdates)

        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        btnUpdates.setOnClickListener {
            startActivity(Intent(this, UpdatesActivity::class.java))
        }
    }
}
