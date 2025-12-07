package com.diajarkoding.duittracker.di

import android.content.Context
import androidx.room.Room
import com.diajarkoding.duittracker.data.local.DuitTrackerDatabase
import com.diajarkoding.duittracker.data.local.dao.PendingOperationDao
import com.diajarkoding.duittracker.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): DuitTrackerDatabase {
        return Room.databaseBuilder(
            context,
            DuitTrackerDatabase::class.java,
            DuitTrackerDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: DuitTrackerDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun providePendingOperationDao(database: DuitTrackerDatabase): PendingOperationDao {
        return database.pendingOperationDao()
    }
}
