package com.example.ngdtechsupport.ui.updates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.databinding.ActivityUpdatesBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.ngdtechsupport.utils.AnalyticsHelper

class UpdatesActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UpdatesActivity"
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private lateinit var binding: ActivityUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()
    private lateinit var adapter: UpdatesAdapter
    private lateinit var companyId: String
    private lateinit var businessId: String
    private lateinit var skeletonUpdatesView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AnalyticsHelper.screenView("UpdatesActivity")
        skeletonUpdatesView = findViewById(R.id.skeletonUpdates)

        setupToolbar()

        companyId = resolveCompanyId(intent.getStringExtra("companyId"))
        businessId = resolveBusinessId(intent.getStringExtra("businessId"))
        val userRole = intent.getStringExtra("userRole").orEmpty().ifEmpty { "CLIENT" }.uppercase()
        Log.d(TAG, "Opening updates with companyId=$companyId businessId=$businessId role=$userRole")

        binding.btnNewUpdate.visibility = if (userRole == "ADMIN") View.VISIBLE else View.GONE
        showLoadingSkeleton()

        adapter = UpdatesAdapter { update ->
            // Navigate to update detail screen
            val intent = Intent(this, UpdateDetailActivity::class.java)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)
            intent.putExtra("updateId", update.id)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            AnalyticsHelper.updateViewed(update.id)
        }

        binding.recyclerUpdates.layoutManager = LinearLayoutManager(this)
        binding.recyclerUpdates.setHasFixedSize(true)
        binding.recyclerUpdates.setItemViewCacheSize(12)
        binding.recyclerUpdates.setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 20)
        })
        binding.recyclerUpdates.adapter = adapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (currentUserId.isNotEmpty()) {
            viewModel.markUpdatesRead(currentUserId)
        } else {
            Log.w(TAG, "No current user when opening updates; markUpdatesRead skipped")
        }

        viewModel.updates.observe(this) { updates ->
            hideLoadingSkeleton()
            binding.progressBar.visibility = View.GONE
            binding.btnRetryUpdates.visibility = View.GONE
            if (updates.isEmpty()) {
                binding.tvEmpty.text = getString(R.string.updates_empty_message)
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerUpdates.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerUpdates.visibility = View.VISIBLE
                adapter.submitList(updates)
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNullOrBlank()) return@observe
            hideLoadingSkeleton()
            binding.progressBar.visibility = View.GONE
            Log.e(TAG, "Error loading updates: $errorMessage")
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            binding.btnRetryUpdates.visibility = View.VISIBLE
            if (adapter.currentList.isEmpty()) {
                binding.tvEmpty.text = getString(R.string.updates_error_message)
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerUpdates.visibility = View.GONE
            }
            viewModel.clearError()
        }

        binding.btnRetryUpdates.setOnClickListener {
            retryLoadUpdates()
        }

        retryLoadUpdates()

        binding.btnNewUpdate.setOnClickListener {
            if (userRole != "ADMIN") {
                Toast.makeText(this, "Solo administradores pueden publicar updates.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CreateUpdatesActivity::class.java)
            intent.putExtra("companyId", companyId)
            intent.putExtra("businessId", businessId)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun retryLoadUpdates() {
        showLoadingSkeleton()
        binding.progressBar.visibility = View.GONE
        binding.btnRetryUpdates.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        if (adapter.currentList.isEmpty()) {
            binding.recyclerUpdates.visibility = View.GONE
        }
        viewModel.listenUpdates(companyId, businessId)
    }

    private fun showLoadingSkeleton() {
        skeletonUpdatesView.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.btnRetryUpdates.visibility = View.GONE
        binding.recyclerUpdates.visibility = View.GONE
    }

    private fun hideLoadingSkeleton() {
        skeletonUpdatesView.visibility = View.GONE
    }

    private fun resolveCompanyId(raw: String?): String {
        val normalized = raw
            .orEmpty()
            .trim()
            .ifBlank { DEFAULT_COMPANY_ID }
            .replace("/", "_")
            .replace("#", "_")
            .replace("?", "_")

        return if (normalized.equals(DEFAULT_COMPANY_ID, ignoreCase = true)) {
            DEFAULT_COMPANY_ID
        } else {
            normalized
        }
    }

    private fun resolveBusinessId(raw: String?): String {
        val base = raw.orEmpty().trim()
        if (base.isBlank()) return DEFAULT_BUSINESS_ID
        if (base.equals(DEFAULT_BUSINESS_ID, ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (base.equals("Restaurante Madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (base.equals("restaurante-madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID

        val normalized = if (base.contains(" ")) {
            base.lowercase().replace(" ", "_")
        } else {
            base
        }

        return normalized
            .replace("/", "_")
            .replace("#", "_")
            .replace("?", "_")
            .ifBlank { DEFAULT_BUSINESS_ID }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.resumeListeners(companyId, businessId)
    }

    override fun onStop() {
        super.onStop()
        viewModel.pauseListeners()
    }
}
