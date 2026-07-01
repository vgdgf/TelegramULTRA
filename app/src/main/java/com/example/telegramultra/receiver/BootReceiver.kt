package com.example.telegramultra.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.telegramultra.service.BackupService
import com.example.telegramultra.util.PrefsManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = PrefsManager(context)
        if (prefs.isServiceEnabled && prefs.isConfigured()) {
            Log.i(TAG, "Boot completed — starting BackupService")
            context.startForegroundService(Intent(context, BackupService::class.java))
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
