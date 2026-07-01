package com.example.telegramultra.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramultra.data.db.entity.FileRecord
import com.example.telegramultra.databinding.ItemFileRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentFilesAdapter :
    ListAdapter<FileRecord, RecentFilesAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemFileRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: FileRecord) {
            binding.tvFileName.text = record.fileName
            binding.tvFileSize.text = formatSize(record.fileSizeBytes)
            binding.tvUploadTime.text = formatTime(record.uploadedAt)
            binding.tvStatus.text = if (record.status == FileRecord.STATUS_SUCCESS) "✓" else "✗"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FileRecord>() {
        override fun areItemsTheSame(a: FileRecord, b: FileRecord) = a.id == b.id
        override fun areContentsTheSame(a: FileRecord, b: FileRecord) = a == b
    }
}
