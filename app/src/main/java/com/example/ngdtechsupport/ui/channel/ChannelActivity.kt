package com.example.ngdtechsupport.ui.channel

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.databinding.ActivityChannelBinding

class ChannelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelBinding
    private val viewModel: ChannelViewModel by viewModels()

    private lateinit var adapter: ChannelAdapter

    private val companyId = "NGDStudios"
    private val businessId = "Restaurant_madrid"
    private val currentUserId = "adminUid123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()

        viewModel.channels.observe(this) { channels ->
            adapter.submitList(channels)
        }

        viewModel.loadChannels(companyId, businessId)
    }

    private fun setupRecycler() {
        adapter = ChannelAdapter(currentUserId) { channel ->
            // Aquí luego abriremos ChatActivity con channel.id
        }

        binding.recyclerChannels.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerChannels.adapter = adapter
    }
}