 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.domain.model.TransactionResult
 import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
 import javax.inject.Inject
 
 class UpdateTransactionUseCase @Inject constructor(
     private val repository: ITransactionRepository
 ) {
     suspend operator fun invoke(transaction: Transaction): TransactionResult<Transaction> {
         return repository.updateTransaction(transaction)
     }
 }
