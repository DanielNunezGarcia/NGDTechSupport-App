package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ngdtechsupport.data.UpdatesRepository
import com.example.ngdtechsupport.databinding.ActivityPublishUpdateBinding
import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PublishUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishUpdateBinding
    private val repository = UpdatesRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityPublishUpdateBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val companyId = intent.getStringExtra("companyId") ?: ""
            val businessId = intent.getStringExtra("businessId") ?: ""

            binding.btnPublish.setOnClickListener {
                try {
                    val title = binding.etTitle.text.toString()
                    val description = binding.etDescription.text.toString()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Título obligatorio", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val update = UpdateModel(
                        id = "",
                        title = title,
                        description = description,
                        type = "info",
                        createdAt = System.currentTimeMillis(),
                        createdBy = uid
                    )

                    lifecycleScope.launch {
                        try {
                            repository.publishUpdate(companyId, businessId, update)

                            Toast.makeText(
                                this@PublishUpdateActivity,
                                "Update publicado",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()
                        } catch (e: Exception) {
                            android.util.Log.e("PublishUpdateActivity", "Error publishing update", e)
                            Toast.makeText(this@PublishUpdateActivity, "Error al publicar update", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PublishUpdateActivity", "Error in publish button click", e)
                    Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PublishUpdateActivity", "Error in onCreate", e)
            finish()
        }
    }
}