package com.example.telegramultra.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.telegramultra.data.db.entity.FileRecord

@Dao
interface FileRecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: FileRecord): Long

    @Query("SELECT EXISTS(SELECT 1 FROM file_records WHERE fileHash = :hash AND status = 'SUCCESS')")
    suspend fun isAlreadyUploaded(hash: String): Boolean

    @Query("SELECT * FROM file_records ORDER BY uploadedAt DESC LIMIT :limit")
    fun getRecentUploads(limit: Int = 50): LiveData<List<FileRecord>>

    @Query("SELECT COUNT(*) FROM file_records WHERE status = 'SUCCESS'")
    fun getTotalUploaded(): LiveData<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM file_records WHERE status = 'SUCCESS'")
    suspend fun getTotalUploadedBytes(): Long

    @Query("DELETE FROM file_records")
    suspend fun clearAll()
}
