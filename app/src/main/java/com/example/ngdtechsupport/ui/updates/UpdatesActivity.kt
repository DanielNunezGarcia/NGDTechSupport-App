package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.example.ngdtechsupport.R

class UpdatesActivity : AppCompatActivity() {

    private lateinit var viewModel: UpdatesViewModel
    private lateinit var adapter: UpdatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_updates)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerUpdates)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UpdatesAdapter(emptyList())

        recyclerView.adapter = adapter

        val companyId = intent.getStringExtra("companyId") ?: return
        val businessId = intent.getStringExtra("businessId") ?: return

        viewModel = ViewModelProvider(this)[UpdatesViewModel::class.java]

        viewModel.updates.observe(this) {

            adapter.updateList(it)
        }

        viewModel.loadUpdates(companyId, businessId)
    }
}