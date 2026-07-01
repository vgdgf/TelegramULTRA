package com.example.telegramultra.upload

import android.util.Log
import com.example.telegramultra.util.PrefsManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class TelegramClient(private val prefs: PrefsManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Upload a file to Telegram via Bot API sendDocument endpoint.
     * Returns true on success, false on any failure.
     */
    fun upload(file: File): Boolean {
        val token = prefs.botToken ?: run {
            Log.e(TAG, "Bot token is not configured")
            return false
        }
        val chatId = prefs.chatId ?: run {
            Log.e(TAG, "Chat ID is not configured")
            return false
        }

        if (!file.exists() || !file.canRead()) {
            Log.e(TAG, "File not accessible: ${file.absolutePath}")
            return false
        }

        val caption = buildCaption(file)
        val mediaType = resolveMediaType(file)

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("caption", caption)
            .addFormDataPart(
                "document",
                file.name,
                file.asRequestBody(mediaType.toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$token/sendDocument")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val ok = json.optBoolean("ok", false)
                if (!ok) Log.e(TAG, "Telegram API error: $responseBody")
                ok
            } else {
                Log.e(TAG, "HTTP ${response.code}: $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed for ${file.name}", e)
            false
        }
    }

    /**
     * Test connection with the configured bot token and chat ID.
     */
    fun testConnection(): Pair<Boolean, String> {
        val token = prefs.botToken ?: return Pair(false, "Bot token not set")
        val chatId = prefs.chatId ?: return Pair(false, "Chat ID not set")

        val body = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", "✅ TelegramULTRA connected successfully!")
            .build()

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$token/sendMessage")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            if (json.optBoolean("ok", false)) {
                Pair(true, "Connection successful")
            } else {
                val desc = json.optJSONObject("parameters")
                    ?.optString("description") ?: json.optString("description", "Unknown error")
                Pair(false, desc)
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection failed")
        }
    }

    private fun buildCaption(file: File): String {
        val sizeKb = file.length() / 1024
        val unit = if (sizeKb > 1024) "${sizeKb / 1024} MB" else "$sizeKb KB"
        return "📁 ${file.name}\n📦 $unit\n🕐 ${java.util.Date()}"
    }

    private fun resolveMediaType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    companion object {
        private const val TAG = "TelegramClient"
    }
}
