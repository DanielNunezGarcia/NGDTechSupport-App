package com.example.ngdtechsupport.ui.updates

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.databinding.ActivityCreateUpdatesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.appbar.MaterialToolbar
class CreateUpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels()

    private lateinit var companyId: String
    private lateinit var businessId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityCreateUpdatesBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Setup toolbar
            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            companyId = intent.getStringExtra("companyId") ?: ""
            businessId = intent.getStringExtra("businessId") ?: ""

            // Configurar Spinner de colores
            val colors = listOf(
                "Azul (#2196F3)",
                "Verde (#4CAF50)",
                "Naranja (#FF9800)",
                "Rojo (#F44336)",
                "Morado (#9C27B0)"
            )
            val colorValues = listOf(
                "#2196F3",
                "#4CAF50",
                "#FF9800",
                "#F44336",
                "#9C27B0"
            )
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerColor.adapter = adapter

            binding.btnPublish.setOnClickListener {
                try {
                    val title = binding.etTitle.text.toString()
                    val description = binding.etDescription.text.toString()
                    val version = binding.etVersion.text.toString()

                    // Obtener prioridad seleccionada
                    val priority = when (binding.rgPriority.checkedRadioButtonId) {
                        R.id.rbPriority1 -> 1
                        R.id.rbPriority2 -> 2
                        R.id.rbPriority3 -> 3
                        R.id.rbPriority4 -> 4
                        else -> 1
                    }

                    // Obtener color seleccionado
                    val selectedColorPosition = binding.spinnerColor.selectedItemPosition
                    val color = colorValues[selectedColorPosition]

                    // Obtener fecha del DatePicker
                    val day = binding.datePicker.dayOfMonth
                    val month = binding.datePicker.month
                    val year = binding.datePicker.year
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, month, day, 0, 0, 0)
                    val publishDate = calendar.timeInMillis

                    viewModel.createUpdate(
                        companyId,
                        businessId,
                        title,
                        description,
                        version,
                        "general",
                        FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        priority = priority,
                        color = color,
                        publishDate = publishDate
                    )

                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                } catch (e: Exception) {
                    android.util.Log.e("CreateUpdatesActivity", "Error in publish button click", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CreateUpdatesActivity", "Error in onCreate", e)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}