package com.example.ngdtechsupport.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.data.AppRepository
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import android.content.Intent
import com.example.ngdtechsupport.ui.auth.LoginActivity

class DashboardActivity : AppCompatActivity() {

    private val repo = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textView = findViewById<TextView>(R.id.tvApps)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val logoutButton = findViewById<Button>(R.id.btnLogout)

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

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }
    }
}
