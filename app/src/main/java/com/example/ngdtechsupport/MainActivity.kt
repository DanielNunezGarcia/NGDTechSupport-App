package com.example.ngdtechsupport

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().signOut()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
