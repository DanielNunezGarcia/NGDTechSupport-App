package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.UpdateRepository
import kotlinx.coroutines.*

class UpdatesActivity : AppCompatActivity() {

    private val repo = UpdateRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updates)

        val textView = findViewById<TextView>(R.id.tvUpdates)

        val appId = intent.getStringExtra("appId")

        if (appId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val updates = repo.getUpdatesForApp(appId)

                if (updates.isNotEmpty()) {
                    textView.text = updates.joinToString("\n\n") {
                        "${it.title}\n${it.description}\n${it.date}"
                    }
                } else {
                    textView.text = "No hay novedades"
                }
            }
        }
    }
}