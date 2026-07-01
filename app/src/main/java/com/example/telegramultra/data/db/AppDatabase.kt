package com.example.telegramultra.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.telegramultra.data.db.dao.FileRecordDao
import com.example.telegramultra.data.db.entity.FileRecord

@Database(entities = [FileRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fileRecordDao(): FileRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ultra_backup.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
