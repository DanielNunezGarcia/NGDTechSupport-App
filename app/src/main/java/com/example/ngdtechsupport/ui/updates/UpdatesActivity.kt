package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityUpdatesBinding
import com.google.firebase.auth.FirebaseAuth

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

        if (companyId.isEmpty() || businessId.isEmpty()) {
            Toast.makeText(this, "Error: Datos no disponibles", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = UpdatesAdapter()

        binding.recyclerUpdates.adapter = adapter
        binding.recyclerUpdates.layoutManager = LinearLayoutManager(this)

        viewModel.updates.observe(this) { updates ->
            adapter.submitList(updates)
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.markUpdatesRead(currentUserId)

        viewModel.listenUpdates(companyId, businessId)
    }
}
