package com.example.ngdtechsupport.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        val progressBar = findViewById<ProgressBar?>(R.id.pbLoading)
        val errorText = findViewById<TextView?>(R.id.tvError)

        // Observamos el estado del ViewModel
        viewModel.uiState.observe(this, Observer { state ->
            when (state) {
                is LoginUiState.Idle -> {
                    progressBar?.visibility = View.GONE
                    errorText?.visibility = View.GONE
                    loginBtn.isEnabled = true
                }
                is LoginUiState.Loading -> {
                    progressBar?.visibility = View.VISIBLE
                    errorText?.visibility = View.GONE
                    loginBtn.isEnabled = false
                }
                is LoginUiState.Success -> {
                    progressBar?.visibility = View.GONE
                    errorText?.visibility = View.GONE
                    loginBtn.isEnabled = true

                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                is LoginUiState.Error -> {
                    progressBar?.visibility = View.GONE
                    loginBtn.isEnabled = true
                    errorText?.apply {
                        visibility = View.VISIBLE
                        text = state.message
                    }
                }
            }
        })

        // Click en el botón de login llama al ViewModel
        loginBtn.setOnClickListener {
            val userEmail = email.text.toString()
            val userPassword = password.text.toString()
            viewModel.login(userEmail, userPassword)
        }
    }
}