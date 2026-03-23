package com.example.ngdtechsupport.ui.channel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth

class ChannelActivity : AppCompatActivity() {

    private lateinit var viewModel: ChannelViewModel
    private lateinit var adapter: ChannelAdapter

    private lateinit var companyId: String
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_channel)
            Log.d("ChannelActivity", "onCreate started")

            companyId = intent.getStringExtra("companyId").orEmpty().ifEmpty { "NGDStudios" }

            viewModel = ViewModelProvider(this)[ChannelViewModel::class.java]

            adapter = ChannelAdapter(
                viewModel = viewModel,
                companyId = companyId,
                currentUserId = currentUserId
            )

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerChannels)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            viewModel.visibleChannels.observe(this) { list ->
                try {
                    Log.d("ChannelActivity", "Visible channels updated: ${list.size}")
                    adapter.submitList(list)
                } catch (e: Exception) {
                    Log.e("ChannelActivity", "Error in visibleChannels observer", e)
                }
            }

            // Observar eventos de click
            viewModel.channelClickEvent.observe(this) { channel ->
                try {
                    channel?.let {
                        Log.d("ChannelActivity", "Channel clicked: ${it.id}")
                        val intent = Intent(this, ChatActivity::class.java).apply {
                            putExtra("companyId", companyId)
                            putExtra("businessId", "")
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
}
