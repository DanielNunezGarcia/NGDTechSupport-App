package com.example.ngdtechsupport.ui.channel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth

class ChannelActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private lateinit var viewModel: ChannelViewModel
    private lateinit var adapter: ChannelAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var initialLoading: ProgressBar
    private lateinit var loadingMore: ProgressBar
    private lateinit var btnLoadMore: Button
    private lateinit var emptyView: TextView

    private lateinit var companyId: String
    private lateinit var businessId: String
    private var userRole: String = "CLIENT"
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_channel)
            Log.d("ChannelActivity", "onCreate started")

            companyId = intent.getStringExtra("companyId").orEmpty().ifEmpty { DEFAULT_COMPANY_ID }
            businessId = intent.getStringExtra("businessId").orEmpty().ifEmpty { DEFAULT_BUSINESS_ID }
            userRole = intent.getStringExtra("userRole").orEmpty().ifEmpty { "CLIENT" }.uppercase()
            Log.d("ChannelActivity", "Resolved scope companyId=$companyId businessId=$businessId role=$userRole")

            viewModel = ViewModelProvider(this)[ChannelViewModel::class.java]

            adapter = ChannelAdapter(
                viewModel = viewModel,
                companyId = companyId,
                currentUserId = currentUserId
            )

            recyclerView = findViewById(R.id.recyclerChannels)
            initialLoading = findViewById(R.id.progressChannelsInitial)
            loadingMore = findViewById(R.id.progressChannelsLoadMore)
            btnLoadMore = findViewById(R.id.btnLoadMoreChannels)
            emptyView = findViewById(R.id.tvChannelsEmpty)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(12)
            recyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                setMaxRecycledViews(0, 20)
            })
            recyclerView.adapter = adapter

            viewModel.visibleChannels.observe(this) { list ->
                try {
                    Log.d("ChannelActivity", "Visible channels updated: ${list.size}")
                    adapter.submitList(list)
                    emptyView.visibility = if (list.isEmpty() && viewModel.isInitialLoading.value != true) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("ChannelActivity", "Error in visibleChannels observer", e)
                }
            }

            viewModel.isInitialLoading.observe(this) { loading ->
                initialLoading.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
                if (loading) {
                    emptyView.visibility = android.view.View.GONE
                    recyclerView.visibility = android.view.View.GONE
                } else {
                    recyclerView.visibility = android.view.View.VISIBLE
                    val shouldShowEmpty = adapter.itemCount == 0
                    emptyView.visibility = if (shouldShowEmpty) android.view.View.VISIBLE else android.view.View.GONE
                }
            }

            viewModel.isLoadingMore.observe(this) { loading ->
                loadingMore.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
                updateLoadMoreButtonVisibility()
            }

            viewModel.canLoadMore.observe(this) {
                updateLoadMoreButtonVisibility()
            }

            viewModel.error.observe(this) { errorMessage ->
                if (errorMessage.isNullOrBlank()) return@observe
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }

            btnLoadMore.setOnClickListener {
                viewModel.loadMoreChannels()
            }

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) return

                    val manager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val totalItems = manager.itemCount
                    val lastVisibleItem = manager.findLastVisibleItemPosition()
                    if (totalItems > 0 && lastVisibleItem >= totalItems - 4) {
                        viewModel.loadMoreChannels()
                    }
                }
            })

            // Observar eventos de click
            viewModel.channelClickEvent.observe(this) { channel ->
                try {
                    channel?.let {
                        Log.d("ChannelActivity", "Channel clicked: ${it.id}")
                        val intent = Intent(this, ChatActivity::class.java).apply {
                            putExtra("companyId", companyId)
                            putExtra("businessId", businessId)
                            putExtra("userRole", userRole)
                            putExtra("channelId", it.id)
                        }
                        startActivity(intent)
                        viewModel.clearChannelClickEvent()
                    }
                } catch (e: Exception) {
                    Log.e("ChannelActivity", "Error in channelClickEvent observer", e)
                }
            }

            viewModel.loadChannels(companyId)
            Log.d("ChannelActivity", "onCreate completed")
        } catch (e: Exception) {
            Log.e("ChannelActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error al cargar canales", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (
            ::viewModel.isInitialized &&
            ::companyId.isInitialized &&
            ::adapter.isInitialized &&
            companyId.isNotBlank() &&
            adapter.itemCount == 0
        ) {
            viewModel.loadChannels(companyId)
        }
    }

    private fun updateLoadMoreButtonVisibility() {
        if (!::viewModel.isInitialized || !::btnLoadMore.isInitialized) return

        val shouldShow = viewModel.canLoadMore.value == true && viewModel.isLoadingMore.value != true
        btnLoadMore.visibility = if (shouldShow) android.view.View.VISIBLE else android.view.View.GONE
    }
}
