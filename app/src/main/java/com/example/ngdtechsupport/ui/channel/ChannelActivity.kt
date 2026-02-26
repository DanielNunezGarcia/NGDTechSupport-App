package com.example.ngdtechsupport.ui.channel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.R

class ChannelActivity : AppCompatActivity() {

    private lateinit var viewModel: ChannelViewModel
    private lateinit var adapter: ChannelAdapter

    private val companyId = "NGDStudios"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)

        viewModel = ViewModelProvider(this)[ChannelViewModel::class.java]

        adapter = ChannelAdapter(
            viewModel = viewModel,
            companyId = companyId
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerChannels)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.visibleChannels.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.loadChannels(companyId)
    }
}