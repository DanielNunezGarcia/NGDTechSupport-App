package com.example.ngdtechsupport.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.AppRepository
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class DashboardActivity : AppCompatActivity() {

    private val repo = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textView = findViewById<TextView>(R.id.tvApps)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val logoutBtn = findViewById<Button>(R.id.btnLogout)

        if (uid != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val apps = repo.getAppsForUser(uid)

                if (apps.isNotEmpty()) {
                    textView.text = apps.joinToString("\n") {
                        "${it.name} - ${it.status}"
                    }
                } else {
                    textView.text = "No tienes apps asignadas"
                }
            }
        }

        logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }
    }
}
