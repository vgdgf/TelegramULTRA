package com.example.telegramultra.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PrefsManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ultra_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var botToken: String?
        get() = prefs.getString(KEY_BOT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_BOT_TOKEN, value).apply()

    var chatId: String?
        get() = prefs.getString(KEY_CHAT_ID, null)
        set(value) = prefs.edit().putString(KEY_CHAT_ID, value).apply()

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    fun isConfigured(): Boolean = !botToken.isNullOrBlank() && !chatId.isNullOrBlank()

    companion object {
        private const val KEY_BOT_TOKEN = "bot_token"
        private const val KEY_CHAT_ID = "chat_id"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }
}
