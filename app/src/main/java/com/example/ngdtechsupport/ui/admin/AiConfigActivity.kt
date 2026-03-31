package com.example.ngdtechsupport.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.R
import com.example.ngdtechsupport.databinding.ActivityAiConfigBinding

class AiConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiConfigBinding
    private val viewModel: AiConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityAiConfigBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupToolbar()

            val companyId = intent.getStringExtra("companyId") ?: "NGDStudios"

            hideWelcomeMessagesSection()
            loadConfig(companyId)
            setupSaveButton(companyId)
            observeViewModel()
        } catch (e: Exception) {
            android.util.Log.e("AiConfigActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error al cargar configuración", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun hideWelcomeMessagesSection() {
        // Hide welcome messages section as it's now automatic
        // Welcome messages are configured in Firestore directly
        // The app will use default messages if none are configured
        try {
            // Hide the entire welcome section
            binding.tvWelcomeTitle.visibility = android.view.View.GONE
            binding.tvWelcomeInstructions.visibility = android.view.View.GONE
            binding.layoutWelcomeMessages.visibility = android.view.View.GONE
            // Also disable editing in case it's shown somehow
            binding.etWelcomeMessages.isEnabled = false
        } catch (e: Exception) {
            // Ignore if the view doesn't exist
        }
    }

    private fun loadConfig(companyId: String) {
        viewModel.loadConfig(companyId)
    }

    private fun setupSaveButton(companyId: String) {
        binding.btnSave.setOnClickListener {
            try {
                val welcomeMessages = binding.etWelcomeMessages.text.toString()
                    .split("\n")
                    .filter { it.isNotBlank() }

                val quickReplies = binding.etQuickReplies.text.toString()
                    .split("\n")
                    .filter { it.isNotBlank() }

                val escalationKeywords = binding.etEscalationKeywords.text.toString()
                    .split("\n")
                    .filter { it.isNotBlank() }

                viewModel.setAiEnabled(binding.switchAiEnabled.isChecked)
                viewModel.setAutoTransferEnabled(binding.switchAutoEscalation.isChecked)
                viewModel.setGreetingMessages(welcomeMessages)
                viewModel.setQuickReplies(quickReplies)
                viewModel.setEscalationKeywords(escalationKeywords)
                viewModel.setFallbackMessage(binding.etFallbackMessage.text.toString())

                viewModel.saveConfig(companyId)
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in save button click", e)
                Toast.makeText(this, "Error al guardar configuración", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            try {
                when (state) {
                    is AiConfigViewModel.UiState.Loading -> {
                        binding.btnSave.isEnabled = false
                    }
                    is AiConfigViewModel.UiState.Success -> {
                        binding.btnSave.isEnabled = true
                    }
                    is AiConfigViewModel.UiState.Error -> {
                        binding.btnSave.isEnabled = true
                        Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in uiState observer", e)
            }
        }

        viewModel.aiEnabled.observe(this) { enabled ->
            try {
                binding.switchAiEnabled.isChecked = enabled
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in aiEnabled observer", e)
            }
        }

        viewModel.autoTransferEnabled.observe(this) { enabled ->
            try {
                binding.switchAutoEscalation.isChecked = enabled
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in autoTransferEnabled observer", e)
            }
        }

        viewModel.greetingMessages.observe(this) { messages ->
            try {
                binding.etWelcomeMessages.setText(messages.joinToString("\n"))
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in greetingMessages observer", e)
            }
        }

        viewModel.quickReplies.observe(this) { replies ->
            try {
                binding.etQuickReplies.setText(replies.joinToString("\n"))
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in quickReplies observer", e)
            }
        }

        viewModel.escalationKeywords.observe(this) { keywords ->
            try {
                binding.etEscalationKeywords.setText(keywords.joinToString("\n"))
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in escalationKeywords observer", e)
            }
        }

        viewModel.fallbackMessage.observe(this) { message ->
            try {
                binding.etFallbackMessage.setText(message)
            } catch (e: Exception) {
                android.util.Log.e("AiConfigActivity", "Error in fallbackMessage observer", e)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
