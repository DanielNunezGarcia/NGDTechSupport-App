package com.example.ngdtechsupport.ui.channel

import android.content.Intent
import android.os.Bundle
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

    private val companyId = "NGDStudios"
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)

        viewModel = ViewModelProvider(this)[ChannelViewModel::class.java]

        adapter = ChannelAdapter(
            viewModel = viewModel,
            companyId = companyId,
            currentUserId = currentUserId
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerChannels)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.visibleChannels.observe(this) { list ->
            adapter.submitList(list)
        }

        // Observar eventos de click
        viewModel.channelClickEvent.observe(this) { channel ->
            channel?.let {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("companyId", companyId)
                    putExtra("businessId", it.id)
                    putExtra("channelId", it.id)
                }
                startActivity(intent)
                viewModel.clearChannelClickEvent()
            }
        }

        viewModel.loadChannels(companyId)
    }
}