package com.example.telegramultra.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.telegramultra.data.db.AppDatabase
import com.example.telegramultra.data.db.entity.FileRecord
import com.example.telegramultra.upload.TelegramClient
import com.example.telegramultra.util.PrefsManager
import java.io.File
import java.util.concurrent.TimeUnit

class UploadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        val fileHash = inputData.getString(KEY_FILE_HASH) ?: return Result.failure()

        val file = File(filePath)
        if (!file.exists()) {
            Log.w(TAG, "File no longer exists: $filePath")
            return Result.failure()
        }

        val prefs = PrefsManager(applicationContext)
        if (!prefs.isConfigured()) {
            Log.e(TAG, "Bot not configured, cannot upload")
            return Result.failure()
        }

        val client = TelegramClient(prefs)
        val success = client.upload(file)

        val db = AppDatabase.getInstance(applicationContext)
        val record = FileRecord(
            filePath = filePath,
            fileName = file.name,
            fileHash = fileHash,
            fileSizeBytes = file.length(),
            status = if (success) FileRecord.STATUS_SUCCESS else FileRecord.STATUS_FAILED
        )

        // Use runBlocking since Worker.doWork() is synchronous
        kotlinx.coroutines.runBlocking {
            db.fileRecordDao().insert(record)
        }

        return if (success) {
            Log.i(TAG, "Uploaded successfully: ${file.name}")
            Result.success()
        } else {
            Log.w(TAG, "Upload failed, will retry: ${file.name}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "UploadWorker"
        private const val KEY_FILE_PATH = "file_path"
        private const val KEY_FILE_HASH = "file_hash"

        fun enqueue(context: Context, filePath: String, fileHash: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = workDataOf(
                KEY_FILE_PATH to filePath,
                KEY_FILE_HASH to fileHash
            )

            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(fileHash, ExistingWorkPolicy.KEEP, request)
        }
    }
}
