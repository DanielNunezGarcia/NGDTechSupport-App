package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R

class CreateUpdateActivity : AppCompatActivity() {

    private val viewModel: UpdatesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_updates)

        val companyId = intent.getStringExtra("companyId") ?: return
        val businessId = intent.getStringExtra("businessId") ?: return

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etVersion = findViewById<EditText>(R.id.etVersion)

        val btnPublish = findViewById<Button>(R.id.btnPublish)

        btnPublish.setOnClickListener {

            val title = etTitle.text.toString()
            val description = etDescription.text.toString()
            val version = etVersion.text.toString()

            viewModel.createUpdate(
                companyId,
                businessId,
                title,
                description,
                version
            )

            finish()
        }
    }
}