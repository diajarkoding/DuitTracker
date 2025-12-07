package com.diajarkoding.duittracker.data.local

import androidx.room.Database
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
    }
}
