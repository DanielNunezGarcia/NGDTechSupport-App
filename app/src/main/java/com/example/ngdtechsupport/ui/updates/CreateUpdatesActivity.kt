package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.databinding.ActivityCreateUpdatesBinding
class CreateUpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()

    private lateinit var companyId: String
    private lateinit var businessId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        binding.btnPublish.setOnClickListener {

            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            val version = binding.etVersion.text.toString()

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