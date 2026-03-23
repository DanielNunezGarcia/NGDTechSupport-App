package com.example.ngdtechsupport.ui.updates

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityUpdatesBinding
import com.google.firebase.auth.FirebaseAuth

class UpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()
    private lateinit var adapter: UpdatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val companyId = intent.getStringExtra("companyId").orEmpty().ifEmpty { "NGDStudios" }
        val businessId = intent.getStringExtra("businessId").orEmpty().ifEmpty { "restaurante_madrid" }

        adapter = UpdatesAdapter { update ->
            Toast.makeText(this, update.title, Toast.LENGTH_SHORT).show()
        }

        binding.recyclerUpdates.layoutManager = LinearLayoutManager(this)
        binding.recyclerUpdates.setHasFixedSize(true)
        binding.recyclerUpdates.adapter = adapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (currentUserId.isNotEmpty()) {
            viewModel.markUpdatesRead(currentUserId)
        }

        viewModel.updates.observe(this) { updates ->
            if (updates.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerUpdates.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerUpdates.visibility = View.VISIBLE
                adapter.submitList(updates)
            }
        }

        viewModel.listenUpdates(companyId, businessId)

        binding.btnNewUpdate.setOnClickListener {
            val intent = Intent(this, CreateUpdatesActivity::class.java)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)
            startActivity(intent)
        }
    }
}
