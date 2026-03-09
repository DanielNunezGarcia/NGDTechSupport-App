package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityUpdatesBinding

class UpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()

    private lateinit var companyId: String
    private lateinit var businessId: String

    private lateinit var adapter: UpdatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        adapter = UpdatesAdapter(emptyList())

        binding.recyclerUpdates.adapter = adapter
        binding.recyclerUpdates.layoutManager =
            LinearLayoutManager(this)

        viewModel.listenUpdates(
            companyId,
            businessId
        )

        viewModel.updates.observe(this) { updates ->

            adapter.updateList(updates)

        }
    }
}