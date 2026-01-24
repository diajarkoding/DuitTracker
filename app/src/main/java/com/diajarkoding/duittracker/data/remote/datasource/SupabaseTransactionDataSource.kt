 package com.diajarkoding.duittracker.data.remote.datasource
 
 import com.diajarkoding.duittracker.data.remote.dto.TransactionDto
 import io.github.jan.supabase.SupabaseClient
 import io.github.jan.supabase.postgrest.postgrest
 import javax.inject.Inject
 import javax.inject.Singleton
 
 /**
  * Supabase implementation of [ITransactionRemoteDataSource].
  * 
  * Handles all network operations with Supabase PostgreSQL database.
  * This implementation can be easily swapped with another backend
  * (Firebase, custom REST API, etc.) by implementing the same interface.
  */
 @Singleton
 class SupabaseTransactionDataSource @Inject constructor(
     private val supabaseClient: SupabaseClient
 ) : ITransactionRemoteDataSource {
     
     companion object {
         private const val TABLE_TRANSACTIONS = "transactions"
     }
     
     override suspend fun getTransactions(userId: String): List<TransactionDto> {
         return supabaseClient.postgrest[TABLE_TRANSACTIONS]
             .select {
                 filter { eq("user_id", userId) }
             }
             .decodeList()
     }
     
     override suspend fun insertTransaction(transaction: TransactionDto) {
         supabaseClient.postgrest[TABLE_TRANSACTIONS].insert(transaction)
     }
     
     override suspend fun updateTransaction(id: String, transaction: TransactionDto) {
         supabaseClient.postgrest[TABLE_TRANSACTIONS]
             .update(transaction) {
                 filter { eq("id", id) }
             }
     }
     
     override suspend fun deleteTransaction(id: String) {
         supabaseClient.postgrest[TABLE_TRANSACTIONS]
             .delete {
                 filter { eq("id", id) }
             }
     }
 }
