package com.example.telegramultra.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.telegramultra.engine.UltraBackupEngine
import com.example.telegramultra.ui.MainActivity

class BackupService : Service() {

    private lateinit var engine: UltraBackupEngine

    override fun onCreate() {
        super.onCreate()
        engine = UltraBackupEngine(this)
        Log.i(TAG, "BackupService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        engine.start()
        Log.i(TAG, "BackupService started")
        return START_STICKY
    }

    override fun onDestroy() {
        engine.stop()
        Log.i(TAG, "BackupService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "ultra_backup_channel"
        val channel = NotificationChannel(
            channelId,
            "Backup Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "TelegramULTRA backup is running"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ULTRA Backup Active")
            .setContentText("Monitoring Telegram media...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "BackupService"
        private const val NOTIFICATION_ID = 1001
    }
}
