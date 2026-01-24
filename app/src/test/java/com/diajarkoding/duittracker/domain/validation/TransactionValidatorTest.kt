 package com.diajarkoding.duittracker.domain.validation
 
 import com.diajarkoding.duittracker.R
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertTrue
 import org.junit.Before
 import org.junit.Test
 
 class TransactionValidatorTest {
 
     private lateinit var validator: TransactionValidator
 
     @Before
     fun setup() {
         validator = TransactionValidator()
     }
 
     // Amount validation tests
     
     @Test
     fun `validateAmount with valid amount should return Valid`() {
         val result = validator.validateAmount(100.0)
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateAmount with minimum valid amount should return Valid`() {
         val result = validator.validateAmount(0.01)
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateAmount with zero should return Invalid`() {
         val result = validator.validateAmount(0.0)
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_amount_too_small, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `validateAmount with negative should return Invalid`() {
         val result = validator.validateAmount(-100.0)
         assertTrue(result is ValidationResult.Invalid)
     }
 
     @Test
     fun `validateAmount with amount exceeding max should return Invalid`() {
         val result = validator.validateAmount(TransactionValidator.MAX_AMOUNT + 1)
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_amount_too_large, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `validateAmount with NaN should return Invalid`() {
         val result = validator.validateAmount(Double.NaN)
         assertTrue(result is ValidationResult.Invalid)
     }
 
     @Test
     fun `validateAmount with Infinity should return Invalid`() {
         val result = validator.validateAmount(Double.POSITIVE_INFINITY)
         assertTrue(result is ValidationResult.Invalid)
     }
 
     // Note validation tests
     
     @Test
     fun `validateNote with valid note should return Valid`() {
         val result = validator.validateNote("Valid note")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateNote with empty note should return Invalid`() {
         val result = validator.validateNote("")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_empty_note, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `validateNote with blank note should return Invalid`() {
         val result = validator.validateNote("   ")
         assertTrue(result is ValidationResult.Invalid)
     }
 
     @Test
     fun `validateNote with note exceeding max length should return Invalid`() {
         val longNote = "a".repeat(TransactionValidator.MAX_NOTE_LENGTH + 1)
         val result = validator.validateNote(longNote)
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_note_too_long, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `validateNote with SQL injection pattern should return Invalid`() {
         val result = validator.validateNote("test; DROP TABLE users;")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_invalid_input, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `validateNote with SELECT pattern should return Invalid`() {
         val result = validator.validateNote("SELECT * FROM transactions")
         assertTrue(result is ValidationResult.Invalid)
     }
 
     // Description validation tests
     
     @Test
     fun `validateDescription with null should return Valid`() {
         val result = validator.validateDescription(null)
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateDescription with empty string should return Valid`() {
         val result = validator.validateDescription("")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateDescription with valid description should return Valid`() {
         val result = validator.validateDescription("This is a valid description")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateDescription with description exceeding max length should return Invalid`() {
         val longDesc = "a".repeat(TransactionValidator.MAX_DESCRIPTION_LENGTH + 1)
         val result = validator.validateDescription(longDesc)
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_description_too_long, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `validateDescription with dangerous patterns should return Invalid`() {
         val result = validator.validateDescription("DELETE FROM users WHERE 1=1")
         assertTrue(result is ValidationResult.Invalid)
     }
 
     // Edge cases
     
     @Test
     fun `validateNote with max length should return Valid`() {
         val maxNote = "a".repeat(TransactionValidator.MAX_NOTE_LENGTH)
         val result = validator.validateNote(maxNote)
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateAmount with max valid amount should return Valid`() {
         val result = validator.validateAmount(TransactionValidator.MAX_AMOUNT)
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateNote with special characters should return Valid`() {
         val result = validator.validateNote("Lunch @ Restaurant #123 - $50")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `validateNote with unicode characters should return Valid`() {
         val result = validator.validateNote("Makan siang 🍔 Rp 50.000")
         assertTrue(result is ValidationResult.Valid)
     }
 }
