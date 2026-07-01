package com.example.telegramultra.engine

import android.content.Context
import android.util.Log
import com.example.telegramultra.data.db.AppDatabase
import com.example.telegramultra.media.MediaScanner
import com.example.telegramultra.queue.EventQueue
import com.example.telegramultra.work.UploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest

class UltraBackupEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scanner = MediaScanner(context)
    private val queue = EventQueue()
    private val db = AppDatabase.getInstance(context)

    fun start() {
        Log.i(TAG, "UltraBackupEngine started")
        scanner.observe { filePath ->
            scope.launch {
                processFile(filePath)
            }
        }
    }

    fun stop() {
        scanner.stop()
        Log.i(TAG, "UltraBackupEngine stopped")
    }

    private suspend fun processFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists() || !file.canRead() || file.length() == 0L) return

        val hash = sha256(file) ?: return

        val alreadyUploaded = db.fileRecordDao().isAlreadyUploaded(hash)
        if (alreadyUploaded) {
            Log.d(TAG, "Skipping already-uploaded file: ${file.name}")
            return
        }

        queue.push(filePath)
        queue.consume { path ->
            UploadWorker.enqueue(context, path, hash)
        }
    }

    private fun sha256(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { stream ->
                val buffer = ByteArray(8192)
                var read: Int
                while (stream.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hash file: ${file.name}", e)
            null
        }
    }

    companion object {
        private const val TAG = "UltraBackupEngine"
    }
}
