package com.diajarkoding.duittracker.di

import com.diajarkoding.duittracker.data.repository.SupabaseAuthRepository
import com.diajarkoding.duittracker.data.repository.TransactionRepositoryImpl
import com.diajarkoding.duittracker.domain.repository.IAuthRepository
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): ITransactionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        supabaseAuthRepository: SupabaseAuthRepository
    ): IAuthRepository
}
