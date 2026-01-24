 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.domain.model.TransactionResult
 import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
 import kotlinx.coroutines.flow.Flow
 import javax.inject.Inject
 
 class GetTransactionsUseCase @Inject constructor(
     private val repository: ITransactionRepository
 ) {
     operator fun invoke(): Flow<TransactionResult<List<Transaction>>> {
         return repository.getAllTransactions()
     }
 }
