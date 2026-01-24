 package com.diajarkoding.duittracker.ui.utils
 
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertTrue
 import org.junit.Test
 
 class AccessibilityUtilsTest {
 
     @Test
     fun `formatCurrencyForAccessibility should format expense correctly`() {
         val result = AccessibilityUtils.formatCurrencyForAccessibility(50000.0, isExpense = true)
         
         assertTrue(result.contains("expense"))
         assertTrue(result.contains("50000"))
         assertTrue(result.contains("rupiah"))
     }
 
     @Test
     fun `formatCurrencyForAccessibility should format income correctly`() {
         val result = AccessibilityUtils.formatCurrencyForAccessibility(100000.0, isExpense = false)
         
         assertTrue(result.contains("income"))
         assertTrue(result.contains("100000"))
         assertTrue(result.contains("rupiah"))
     }
 
     @Test
     fun `formatCurrencyForAccessibility should handle zero amount`() {
         val result = AccessibilityUtils.formatCurrencyForAccessibility(0.0, isExpense = true)
         
         assertTrue(result.contains("0"))
     }
 
     @Test
     fun `formatTransactionForAccessibility should include all details for expense`() {
         val result = AccessibilityUtils.formatTransactionForAccessibility(
             category = "Food",
             amount = 25000.0,
             note = "Lunch at restaurant",
             isExpense = true
         )
         
         assertTrue(result.contains("Expense"))
         assertTrue(result.contains("Food"))
         assertTrue(result.contains("25000"))
         assertTrue(result.contains("Lunch at restaurant"))
     }
 
     @Test
     fun `formatTransactionForAccessibility should include all details for income`() {
         val result = AccessibilityUtils.formatTransactionForAccessibility(
             category = "Salary",
             amount = 5000000.0,
             note = "Monthly salary",
             isExpense = false
         )
         
         assertTrue(result.contains("Income"))
         assertTrue(result.contains("Salary"))
         assertTrue(result.contains("5000000"))
         assertTrue(result.contains("Monthly salary"))
     }
 
     @Test
     fun `MIN_TOUCH_TARGET_SIZE should be 48dp as per Material guidelines`() {
         assertEquals(48, AccessibilityUtils.MIN_TOUCH_TARGET_SIZE)
     }
 }
