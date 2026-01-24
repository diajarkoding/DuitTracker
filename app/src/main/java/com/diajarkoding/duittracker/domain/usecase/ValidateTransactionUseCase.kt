 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.Transaction
 import javax.inject.Inject
 
 sealed class ValidationResult {
     data object Valid : ValidationResult()
     data class Invalid(val errorResId: Int) : ValidationResult()
 }
 
 class ValidateTransactionUseCase @Inject constructor() {
     operator fun invoke(amount: Double, note: String): ValidationResult {
         return when {
             amount <= 0 -> ValidationResult.Invalid(
                 com.diajarkoding.duittracker.R.string.error_invalid_amount
             )
             note.isBlank() -> ValidationResult.Invalid(
                 com.diajarkoding.duittracker.R.string.error_empty_note
             )
             else -> ValidationResult.Valid
         }
     }
 }
