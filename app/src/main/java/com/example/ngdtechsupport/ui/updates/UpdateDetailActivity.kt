package com.example.ngdtechsupport.ui.updates

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.databinding.ActivityUpdateDetailBinding
import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class UpdateDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UpdateDetailActivity"
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private lateinit var binding: ActivityUpdateDetailBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private lateinit var companyId: String
    private lateinit var businessId: String
    private lateinit var updateId: String
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getStringExtra("companyId") ?: DEFAULT_COMPANY_ID
        businessId = intent.getStringExtra("businessId") ?: DEFAULT_BUSINESS_ID
        updateId = intent.getStringExtra("updateId") ?: ""

        // Check if user is admin
        isAdmin = checkIfUserIsAdmin()

        setupToolbar()
        setupAdminActions()
        loadUpdateDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupAdminActions() {
        if (isAdmin) {
            binding.layoutAdminActions.visibility = View.VISIBLE
            
            binding.btnEditUpdate.setOnClickListener {
                // TODO: Implement edit functionality
                Toast.makeText(this, "Edición de updates próximamente", Toast.LENGTH_SHORT).show()
            }

            binding.btnDeleteUpdate.setOnClickListener {
                showDeleteConfirmation()
            }
        } else {
            binding.layoutAdminActions.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Update")
            .setMessage("¿Estás seguro de que quieres eliminar este update? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUpdate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                firestore.collection("companies")
                    .document(companyId)
                    .collection("businesses")
                    .document(businessId)
                    .collection("updates")
                    .document(updateId)
                    .delete()
                    .await()

                Toast.makeText(this@UpdateDetailActivity, "Update eliminado", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting update", e)
                Toast.makeText(this@UpdateDetailActivity, "Error al eliminar update", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUpdateDetails() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val doc = firestore.collection("companies")
                    .document(companyId)
                    .collection("businesses")
                    .document(businessId)
                    .collection("updates")
                    .document(updateId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val update = doc.toObject(UpdateModel::class.java)
                    if (update != null) {
                        displayUpdateDetails(update)
                    } else {
                        Toast.makeText(this@UpdateDetailActivity, "Error al cargar update", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@UpdateDetailActivity, "Update no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading update details", e)
                Toast.makeText(this@UpdateDetailActivity, "Error al cargar detalles", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayUpdateDetails(update: UpdateModel) {
        binding.tvUpdateTitle.text = update.title
        binding.tvDescription.text = update.description
        binding.tvVersion.text = "Versión: ${update.version}"
        
        val priorityText = when (update.priority) {
            1 -> "Prioridad: Baja"
            2 -> "Prioridad: Media"
            3 -> "Prioridad: Alta"
            4 -> "Prioridad: Crítica"
            else -> "Prioridad: Baja"
        }
        binding.tvPriority.text = priorityText
        
        if (update.createdAt > 0) {
            val date = java.util.Date(update.createdAt)
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            binding.tvDate.text = "Fecha: ${dateFormat.format(date)}"
        } else {
            binding.tvDate.text = "Fecha: No disponible"
        }
    }

    private fun checkIfUserIsAdmin(): Boolean {
        val user = currentUser ?: return false
        // For now, we'll assume the user is admin if they have a specific email
        // In a real app, you would check the user's role in Firestore
        return user.email?.contains("admin") == true || 
               user.email?.contains("test") == true ||
               user.email == "admin@test.com" ||
               user.email == "admin@ngd.com"
    }
}