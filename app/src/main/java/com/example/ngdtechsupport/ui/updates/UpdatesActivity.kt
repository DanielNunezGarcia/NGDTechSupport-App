package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.databinding.ActivityUpdatesBinding
import com.google.firebase.auth.FirebaseAuth

class UpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()
    private lateinit var adapter: UpdatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityUpdatesBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            android.util.Log.e("UpdatesActivity", "Error inflating layout", e)
            finish()
            return
        }

        try {
            val companyId = intent.getStringExtra("companyId") ?: ""
            val businessId = intent.getStringExtra("businessId") ?: ""
            android.util.Log.d("UpdatesActivity", "companyId: $companyId, businessId: $businessId")

            if (companyId.isEmpty()) {
                android.util.Log.e("UpdatesActivity", "companyId is empty")
                Toast.makeText(this, "Error: CompanyId no disponible", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            if (businessId.isEmpty()) {
                android.util.Log.e("UpdatesActivity", "businessId is empty")
                Toast.makeText(this, "Error: BusinessId no disponible", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            val finalBusinessId = businessId
            android.util.Log.d("UpdatesActivity", "finalBusinessId: $finalBusinessId")

            adapter = UpdatesAdapter { update ->
                Toast.makeText(this, update.title, Toast.LENGTH_SHORT).show()
            }

            binding.recyclerUpdates.layoutManager = LinearLayoutManager(this)
            binding.recyclerUpdates.adapter = adapter

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (currentUserId.isNotEmpty()) {
                viewModel.markUpdatesRead(currentUserId)
            }

            viewModel.updates.observe(this) { updates ->
                try {
                    if (updates.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.recyclerUpdates.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.recyclerUpdates.visibility = View.VISIBLE
                        adapter.submitList(updates)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UpdatesActivity", "Error in updates observer", e)
                }
            }

            viewModel.listenUpdates(companyId, finalBusinessId)
        } catch (e: Exception) {
            android.util.Log.e("UpdatesActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error al cargar novedades", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
