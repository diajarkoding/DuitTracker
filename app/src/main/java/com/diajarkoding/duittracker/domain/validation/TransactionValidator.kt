 package com.diajarkoding.duittracker.domain.validation
 
 import com.diajarkoding.duittracker.R
 import com.diajarkoding.duittracker.data.model.Transaction
 import javax.inject.Inject
 import javax.inject.Singleton
 
 /**
  * Comprehensive transaction validation.
  * 
  * Validates all transaction fields according to business rules:
  * - Amount must be positive and within reasonable limits
  * - Note must not be empty and must be within length limits
  * - Description length limits
  * - Security checks for injection prevention
  */
 @Singleton
 class TransactionValidator @Inject constructor() {
     
     companion object {
         /** Maximum allowed transaction amount (1 trillion) */
         const val MAX_AMOUNT = 1_000_000_000_000.0
         
         /** Minimum allowed transaction amount */
         const val MIN_AMOUNT = 0.01
         
         /** Maximum note length */
         const val MAX_NOTE_LENGTH = 200
         
         /** Maximum description length */
         const val MAX_DESCRIPTION_LENGTH = 500
         
         /** Characters that could indicate SQL injection attempt */
         private val DANGEROUS_PATTERNS = listOf(
             "DROP TABLE", "DELETE FROM", "INSERT INTO", 
             "UPDATE SET", "SELECT *", "--", "/*", "*/"
         )
     }
     
     /**
      * Validates a complete transaction object.
      * 
      * @param transaction The transaction to validate
      * @return ValidationResult indicating success or specific error
      */
     fun validate(transaction: Transaction): ValidationResult {
         return validateAmount(transaction.amount)
             .takeIf { it !is ValidationResult.Valid }
             ?: validateNote(transaction.note)
                 .takeIf { it !is ValidationResult.Valid }
             ?: validateDescription(transaction.description)
                 .takeIf { it !is ValidationResult.Valid }
             ?: ValidationResult.Valid
     }
     
     /**
      * Validates transaction amount.
      * 
      * @param amount The amount to validate
      * @return ValidationResult
      */
     fun validateAmount(amount: Double): ValidationResult {
         return when {
             amount < MIN_AMOUNT -> ValidationResult.Invalid(
                 R.string.error_amount_too_small,
                 "Amount must be at least $MIN_AMOUNT"
             )
             amount > MAX_AMOUNT -> ValidationResult.Invalid(
                 R.string.error_amount_too_large,
                 "Amount cannot exceed $MAX_AMOUNT"
             )
             amount.isNaN() || amount.isInfinite() -> ValidationResult.Invalid(
                 R.string.error_invalid_amount,
                 "Invalid amount value"
             )
             else -> ValidationResult.Valid
         }
     }
     
     /**
      * Validates transaction note.
      * 
      * @param note The note to validate
      * @return ValidationResult
      */
     fun validateNote(note: String): ValidationResult {
         return when {
             note.isBlank() -> ValidationResult.Invalid(
                 R.string.error_empty_note,
                 "Note cannot be empty"
             )
             note.length > MAX_NOTE_LENGTH -> ValidationResult.Invalid(
                 R.string.error_note_too_long,
                 "Note cannot exceed $MAX_NOTE_LENGTH characters"
             )
             containsDangerousPatterns(note) -> ValidationResult.Invalid(
                 R.string.error_invalid_input,
                 "Invalid characters in note"
             )
             else -> ValidationResult.Valid
         }
     }
     
     /**
      * Validates optional transaction description.
      * 
      * @param description The description to validate (nullable)
      * @return ValidationResult
      */
     fun validateDescription(description: String?): ValidationResult {
         if (description.isNullOrBlank()) return ValidationResult.Valid
         
         return when {
             description.length > MAX_DESCRIPTION_LENGTH -> ValidationResult.Invalid(
                 R.string.error_description_too_long,
                 "Description cannot exceed $MAX_DESCRIPTION_LENGTH characters"
             )
             containsDangerousPatterns(description) -> ValidationResult.Invalid(
                 R.string.error_invalid_input,
                 "Invalid characters in description"
             )
             else -> ValidationResult.Valid
         }
     }
     
     /**
      * Checks for potentially dangerous SQL injection patterns.
      * 
      * @param input The string to check
      * @return true if dangerous patterns are found
      */
     private fun containsDangerousPatterns(input: String): Boolean {
         val upperInput = input.uppercase()
         return DANGEROUS_PATTERNS.any { pattern -> 
             upperInput.contains(pattern.uppercase()) 
         }
     }
 }
 
 /**
  * Represents the result of input validation.
  */
 sealed class ValidationResult {
     /** Input is valid */
     data object Valid : ValidationResult()
     
     /** Input is invalid with specific error */
     data class Invalid(
         val errorResId: Int,
         val errorMessage: String
     ) : ValidationResult()
 }
