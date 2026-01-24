 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.R
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertTrue
 import org.junit.Before
 import org.junit.Test
 
 class ValidateTransactionUseCaseTest {
 
     private lateinit var useCase: ValidateTransactionUseCase
 
     @Before
     fun setup() {
         useCase = ValidateTransactionUseCase()
     }
 
     @Test
     fun `invoke with valid amount and note should return Valid`() {
         val result = useCase(amount = 1000.0, note = "Test note")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `invoke with zero amount should return Invalid with error_invalid_amount`() {
         val result = useCase(amount = 0.0, note = "Test note")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_invalid_amount, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `invoke with negative amount should return Invalid with error_invalid_amount`() {
         val result = useCase(amount = -100.0, note = "Test note")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_invalid_amount, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `invoke with empty note should return Invalid with error_empty_note`() {
         val result = useCase(amount = 1000.0, note = "")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_empty_note, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `invoke with blank note should return Invalid with error_empty_note`() {
         val result = useCase(amount = 1000.0, note = "   ")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_empty_note, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `invoke with zero amount takes priority over empty note`() {
         val result = useCase(amount = 0.0, note = "")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_invalid_amount, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `invoke with very small positive amount should return Valid`() {
         val result = useCase(amount = 0.01, note = "Small amount")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `invoke with very large amount should return Valid`() {
         val result = useCase(amount = 999999999.99, note = "Large amount")
         assertTrue(result is ValidationResult.Valid)
     }
 
     @Test
     fun `invoke with note containing only whitespace should return Invalid`() {
         val result = useCase(amount = 100.0, note = "\t\n  ")
         assertTrue(result is ValidationResult.Invalid)
         assertEquals(R.string.error_empty_note, (result as ValidationResult.Invalid).errorResId)
     }
 
     @Test
     fun `invoke with special characters in note should return Valid`() {
         val result = useCase(amount = 100.0, note = "Test @#$% note!")
         assertTrue(result is ValidationResult.Valid)
     }
 }
