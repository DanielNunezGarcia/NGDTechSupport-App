package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityUpdatesBinding

class UpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()

    private lateinit var adapter: UpdateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UpdateAdapter(emptyList())

        binding.recyclerUpdates.layoutManager = LinearLayoutManager(this)
        binding.recyclerUpdates.adapter = adapter

        observeViewModel()

        val companyId = intent.getStringExtra("companyId") ?: ""
        val businessId = intent.getStringExtra("businessId") ?: ""

        viewModel.loadUpdates(companyId, businessId)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->

            binding.progressBar.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE

            adapter.updateList(state.updates)
        }
    }
}