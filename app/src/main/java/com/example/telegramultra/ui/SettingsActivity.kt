package com.example.telegramultra.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.telegramultra.databinding.ActivitySettingsBinding
import com.example.telegramultra.upload.TelegramClient
import com.example.telegramultra.util.PrefsManager
import kotlinx.coroutines.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        prefs = PrefsManager(this)
        loadSavedValues()
        setupListeners()
    }

    private fun loadSavedValues() {
        binding.etBotToken.setText(prefs.botToken ?: "")
        binding.etChatId.setText(prefs.chatId ?: "")
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val token = binding.etBotToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()

            if (token.isEmpty()) {
                binding.tilBotToken.error = getString(R.string.error_required)
                return@setOnClickListener
            }
            if (chatId.isEmpty()) {
                binding.tilChatId.error = getString(R.string.error_required)
                return@setOnClickListener
            }

            binding.tilBotToken.error = null
            binding.tilChatId.error = null

            prefs.botToken = token
            prefs.chatId = chatId

            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        }

        binding.btnTest.setOnClickListener {
            val token = binding.etBotToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()

            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.botToken = token
            prefs.chatId = chatId

            binding.btnTest.isEnabled = false
            binding.progressTest.visibility = View.VISIBLE

            scope.launch {
                val client = TelegramClient(prefs)
                val (success, message) = withContext(Dispatchers.IO) { client.testConnection() }

                binding.btnTest.isEnabled = true
                binding.progressTest.visibility = View.GONE

                if (success) {
                    Toast.makeText(this@SettingsActivity,
                        getString(R.string.test_success), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SettingsActivity,
                        getString(R.string.test_failed, message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
