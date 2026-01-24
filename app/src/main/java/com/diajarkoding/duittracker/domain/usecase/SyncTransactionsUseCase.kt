 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.domain.model.TransactionResult
 import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
 import javax.inject.Inject
 
 class SyncTransactionsUseCase @Inject constructor(
     private val repository: ITransactionRepository
 ) {
     suspend operator fun invoke(): TransactionResult<Int> {
         return repository.syncPendingOperations()
     }
 }
