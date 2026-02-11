package com.example.ngdtechsupport.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.dashboard.DashboardActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        loginBtn.setOnClickListener {

            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnSuccessListener {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
        }
    }
}