 package com.diajarkoding.duittracker.domain.usecase
 
 import androidx.paging.Pager
 import androidx.paging.PagingConfig
 import androidx.paging.PagingData
 import androidx.paging.map
 import com.diajarkoding.duittracker.data.local.dao.TransactionDao
 import com.diajarkoding.duittracker.data.local.preferences.SyncPreferences
 import com.diajarkoding.duittracker.data.mapper.TransactionMapper.toDomain
 import com.diajarkoding.duittracker.data.model.Transaction
 import kotlinx.coroutines.flow.Flow
 import kotlinx.coroutines.flow.first
 import kotlinx.coroutines.flow.map
 import javax.inject.Inject
 
 class GetPagedTransactionsUseCase @Inject constructor(
     private val transactionDao: TransactionDao,
     private val syncPreferences: SyncPreferences
 ) {
     companion object {
         private const val PAGE_SIZE = 20
         private const val PREFETCH_DISTANCE = 5
     }
 
     suspend operator fun invoke(): Flow<PagingData<Transaction>> {
         val userId = syncPreferences.currentUserId.first() ?: ""
         
         return Pager(
             config = PagingConfig(
                 pageSize = PAGE_SIZE,
                 prefetchDistance = PREFETCH_DISTANCE,
                 enablePlaceholders = false
             ),
             pagingSourceFactory = { transactionDao.getTransactionsPaged(userId) }
         ).flow.map { pagingData ->
             pagingData.map { entity -> entity.toDomain() }
         }
     }
 
     suspend fun getByMonth(monthPrefix: String): Flow<PagingData<Transaction>> {
         val userId = syncPreferences.currentUserId.first() ?: ""
         
         return Pager(
             config = PagingConfig(
                 pageSize = PAGE_SIZE,
                 prefetchDistance = PREFETCH_DISTANCE,
                 enablePlaceholders = false
             ),
             pagingSourceFactory = { transactionDao.getTransactionsByMonthPaged(userId, monthPrefix) }
         ).flow.map { pagingData ->
             pagingData.map { entity -> entity.toDomain() }
         }
     }
 }
