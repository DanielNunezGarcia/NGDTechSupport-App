package com.example.ngdtechsupport.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ngdtechsupport.databinding.ActivityAiConfigBinding

class AiConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiConfigBinding
    private val viewModel: AiConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val companyId = intent.getStringExtra("companyId") ?: "NGDStudios"

        loadConfig(companyId)
        setupSaveButton(companyId)
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadConfig(companyId: String) {
        viewModel.loadConfig(companyId)
    }

    private fun setupSaveButton(companyId: String) {
        binding.btnSave.setOnClickListener {
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
            viewModel.setAutoGreeting(binding.switchAutoWelcome.isChecked)
            viewModel.setAutoTransferEnabled(binding.switchAutoEscalation.isChecked)
            viewModel.setGreetingMessages(welcomeMessages)
            viewModel.setQuickReplies(quickReplies)
            viewModel.setEscalationKeywords(escalationKeywords)
            viewModel.setFallbackMessage(binding.etFallbackMessage.text.toString())

            viewModel.saveConfig(companyId)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
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
        }

        viewModel.aiEnabled.observe(this) { enabled ->
            binding.switchAiEnabled.isChecked = enabled
        }

        viewModel.autoGreeting.observe(this) { enabled ->
            binding.switchAutoWelcome.isChecked = enabled
        }

        viewModel.autoTransferEnabled.observe(this) { enabled ->
            binding.switchAutoEscalation.isChecked = enabled
        }

        viewModel.greetingMessages.observe(this) { messages ->
            binding.etWelcomeMessages.setText(messages.joinToString("\n"))
        }

        viewModel.quickReplies.observe(this) { replies ->
            binding.etQuickReplies.setText(replies.joinToString("\n"))
        }

        viewModel.escalationKeywords.observe(this) { keywords ->
            binding.etEscalationKeywords.setText(keywords.joinToString("\n"))
        }

        viewModel.fallbackMessage.observe(this) { message ->
            binding.etFallbackMessage.setText(message)
        }
    }
}
