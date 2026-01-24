 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.domain.model.TransactionResult
 import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
 import javax.inject.Inject
 
 class GetTransactionByIdUseCase @Inject constructor(
     private val repository: ITransactionRepository
 ) {
     suspend operator fun invoke(transactionId: String): TransactionResult<Transaction?> {
         return repository.getTransactionById(transactionId)
     }
 }
