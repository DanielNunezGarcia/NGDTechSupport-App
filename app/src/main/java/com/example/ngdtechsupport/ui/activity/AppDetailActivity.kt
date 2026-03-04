package com.example.ngdtechsupport.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R

class AppDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        val businessId = intent.getStringExtra("businessId")
        val companyId = intent.getStringExtra("companyId")
    }
}