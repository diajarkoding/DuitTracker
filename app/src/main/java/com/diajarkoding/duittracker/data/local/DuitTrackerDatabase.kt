package com.diajarkoding.duittracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.diajarkoding.duittracker.data.local.dao.PendingOperationDao
import com.diajarkoding.duittracker.data.local.dao.TransactionDao
import com.diajarkoding.duittracker.data.local.entity.PendingOperationEntity
import com.diajarkoding.duittracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        PendingOperationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DuitTrackerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        const val DATABASE_NAME = "duittracker_db"

        @Volatile
        private var INSTANCE: DuitTrackerDatabase? = null

        fun getDatabase(context: Context): DuitTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DuitTrackerDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
