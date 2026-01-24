 package com.diajarkoding.duittracker.data.remote.datasource
 
 import com.diajarkoding.duittracker.data.remote.dto.TransactionDto
 
 /**
  * Interface for remote transaction data operations.
  * Abstracts the network layer (Supabase) from the repository.
  * 
  * This allows for:
  * - Easy testing with mock implementations
  * - Swapping backend providers without changing repository logic
  * - Clear separation of concerns
  */
 interface ITransactionRemoteDataSource {
     
     /**
      * Fetches all transactions for a specific user from remote database.
      * 
      * @param userId The user's unique identifier
      * @return List of transaction DTOs from the remote source
      * @throws Exception if network request fails
      */
     suspend fun getTransactions(userId: String): List<TransactionDto>
     
     /**
      * Inserts a new transaction to remote database.
      * 
      * @param transaction The transaction DTO to insert
      * @throws Exception if network request fails
      */
     suspend fun insertTransaction(transaction: TransactionDto)
     
     /**
      * Updates an existing transaction in remote database.
      * 
      * @param id The transaction ID to update
      * @param transaction The updated transaction DTO
      * @throws Exception if network request fails
      */
     suspend fun updateTransaction(id: String, transaction: TransactionDto)
     
     /**
      * Deletes a transaction from remote database.
      * 
      * @param id The transaction ID to delete
      * @throws Exception if network request fails
      */
     suspend fun deleteTransaction(id: String)
 }
