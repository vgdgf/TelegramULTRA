package com.example.telegramultra.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.telegramultra.data.db.AppDatabase
import com.example.telegramultra.databinding.ActivityMainBinding
import com.example.telegramultra.service.BackupService
import com.example.telegramultra.util.PrefsManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager
    private lateinit var adapter: RecentFilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        requestPermissions()
        refreshUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun setupRecyclerView() {
        adapter = RecentFilesAdapter()
        binding.rvRecentFiles.layoutManager = LinearLayoutManager(this)
        binding.rvRecentFiles.adapter = adapter
    }

    private fun setupObservers() {
        val db = AppDatabase.getInstance(this)
        db.fileRecordDao().getRecentUploads(30).observe(this, Observer { files ->
            adapter.submitList(files)
            binding.tvEmptyState.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        })
        db.fileRecordDao().getTotalUploaded().observe(this, Observer { count ->
            binding.tvTotalFiles.text = count.toString()
        })
    }

    private fun setupListeners() {
        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            prefs.isServiceEnabled = isChecked
            if (isChecked) {
                if (!prefs.isConfigured()) {
                    binding.switchService.isChecked = false
                    prefs.isServiceEnabled = false
                    openSettings()
                    return@setOnCheckedChangeListener
                }
                startForegroundService(Intent(this, BackupService::class.java))
            } else {
                stopService(Intent(this, BackupService::class.java))
            }
            refreshUI()
        }

        binding.btnSettings.setOnClickListener { openSettings() }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun refreshUI() {
        val configured = prefs.isConfigured()
        val enabled = prefs.isServiceEnabled

        binding.switchService.isChecked = enabled
        binding.tvStatus.text = when {
            !configured -> getString(R.string.status_not_configured)
            enabled -> getString(R.string.status_running)
            else -> getString(R.string.status_stopped)
        }
        binding.tvStatusIndicator.setBackgroundResource(
            if (enabled && configured) R.drawable.dot_green else R.drawable.dot_red
        )
        binding.cardWarning.visibility = if (!configured) View.VISIBLE else View.GONE
    }

    private fun requestPermissions() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(Manifest.permission.READ_MEDIA_IMAGES))
                perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            if (!hasPermission(Manifest.permission.READ_MEDIA_VIDEO))
                perms.add(Manifest.permission.READ_MEDIA_VIDEO)
            if (!hasPermission(Manifest.permission.READ_MEDIA_AUDIO))
                perms.add(Manifest.permission.READ_MEDIA_AUDIO)
            if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS))
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), 100)
        }
    }

    private fun hasPermission(perm: String) =
        ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
}
