package com.example.telegramultra.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "file_records",
    indices = [Index(value = ["fileHash"], unique = true)]
)
data class FileRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val fileHash: String,
    val fileSizeBytes: Long,
    val uploadedAt: Long = System.currentTimeMillis(),
    val status: String = STATUS_SUCCESS
) {
    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILED = "FAILED"
    }
}
