package com.example.ngdtechsupport.ui.channel

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ngdtechsupport.data.repository.ChatRepository
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
            adapter.submitList(channels) {
                binding.recyclerChannels.scheduleLayoutAnimation()
            }
        }

        viewModel.totalUnread.observe(this) { total ->
            supportActionBar?.title = "Canales ($total)"
        }

        viewModel.loadChannels(companyId, businessId, currentUserId)

        attachSwipe()
    }

    private fun setupRecycler() {
        adapter = ChannelAdapter(currentUserId) { channel ->
            // Aquí luego abriremos ChatActivity con channel.id
        }

        binding.recyclerChannels.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerChannels.adapter = adapter

        binding.recyclerChannels.itemAnimator =
            androidx.recyclerview.widget.DefaultItemAnimator()

        binding.recyclerChannels.layoutAnimation =
            android.view.animation.AnimationUtils.loadLayoutAnimation(
                this,
                android.R.anim.slide_in_left
            )
    }

    private fun attachSwipe() {

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    target: androidx.recyclerview.widget.RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    direction: Int
                ) {

                    val channel = adapter.currentList[viewHolder.adapterPosition]

                    val repository = ChatRepository()

                    if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                        repository.setChannelPinned(companyId, businessId, channel.id, true)
                    } else {
                        repository.setChannelPinned(companyId, businessId, channel.id, false)
                    }
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(binding.recyclerChannels)
    }
}