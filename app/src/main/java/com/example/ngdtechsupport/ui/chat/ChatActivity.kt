package com.example.ngdtechsupport.ui.chat

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ngdtechsupport.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var currentUserId: String = ""
    private lateinit var companyId: String
    private lateinit var businessId: String

    private var isUserAtBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        companyId = intent.getStringExtra("companyId") ?: ""
        businessId = intent.getStringExtra("businessId") ?: ""

        setupRecycler()
        observeMessages()

        viewModel.loadInitial(companyId, businessId)
    }

    private fun setupRecycler() {

        adapter = ChatAdapter(currentUserId)

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.adapter = adapter

        binding.recyclerViewChat.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {

                val firstVisible =
                    layoutManager.findFirstVisibleItemPosition()

                val lastVisible =
                    layoutManager.findLastVisibleItemPosition()

                val total = layoutManager.itemCount

                if (firstVisible == 0) {
                    viewModel.loadMore(companyId, businessId)
                }

                isUserAtBottom = lastVisible >= total - 2
            }
        })
    }

    private fun observeMessages() {

        viewModel.messages.observe(this) { messages ->

            adapter.submitMessages(messages)

            if (isUserAtBottom) {
                binding.recyclerViewChat.scrollToPosition(
                    adapter.itemCount - 1
                )
            }
        }
    }
}