 package com.diajarkoding.duittracker.utils
 
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertTrue
 import org.junit.Test
 
 class CurrencyFormatterTest {
 
     @Test
     fun `format should return currency with Rp prefix`() {
         val result = CurrencyFormatter.format(1000.0)
         assertTrue(result.contains("Rp"))
     }
 
     @Test
     fun `format should handle zero amount`() {
         val result = CurrencyFormatter.format(0.0)
         assertTrue(result.contains("Rp"))
         assertTrue(result.contains("0"))
     }
 
     @Test
     fun `format should handle large amounts`() {
         val result = CurrencyFormatter.format(1000000.0)
         assertTrue(result.contains("Rp"))
         assertTrue(result.contains("1.000.000") || result.contains("1,000,000"))
     }
 
     @Test
     fun `format should remove trailing zeros`() {
         val result = CurrencyFormatter.format(5000.0)
         assertTrue(!result.contains(",00"))
     }
 
     @Test
     fun `formatCompact should return billions with B suffix`() {
         val result = CurrencyFormatter.formatCompact(1_500_000_000.0)
         assertTrue(result.contains("B"))
         assertTrue(result.contains("1.5") || result.contains("1,5"))
     }
 
     @Test
     fun `formatCompact should return millions with M suffix`() {
         val result = CurrencyFormatter.formatCompact(2_500_000.0)
         assertTrue(result.contains("M"))
         assertTrue(result.contains("2.5") || result.contains("2,5"))
     }
 
     @Test
     fun `formatCompact should return thousands with K suffix`() {
         val result = CurrencyFormatter.formatCompact(5_000.0)
         assertTrue(result.contains("K"))
         assertTrue(result.contains("5.0") || result.contains("5,0"))
     }
 
     @Test
     fun `formatCompact should return regular format for small amounts`() {
         val result = CurrencyFormatter.formatCompact(500.0)
         assertTrue(result.contains("Rp"))
         assertTrue(!result.contains("K") && !result.contains("M") && !result.contains("B"))
     }
 
     @Test
     fun `formatCompact should handle zero`() {
         val result = CurrencyFormatter.formatCompact(0.0)
         assertTrue(result.contains("Rp"))
     }
 
     @Test
     fun `formatCompact should handle exactly one million`() {
         val result = CurrencyFormatter.formatCompact(1_000_000.0)
         assertTrue(result.contains("M"))
     }
 
     @Test
     fun `formatCompact should handle exactly one billion`() {
         val result = CurrencyFormatter.formatCompact(1_000_000_000.0)
         assertTrue(result.contains("B"))
     }
 
     @Test
     fun `formatWithSign should add minus sign for expense`() {
         val result = CurrencyFormatter.formatWithSign(1000.0, isExpense = true)
         assertTrue(result.startsWith("-"))
     }
 
     @Test
     fun `formatWithSign should add plus sign for income`() {
         val result = CurrencyFormatter.formatWithSign(1000.0, isExpense = false)
         assertTrue(result.startsWith("+"))
     }
 
     @Test
     fun `formatWithSign should contain formatted amount`() {
         val result = CurrencyFormatter.formatWithSign(1000.0, isExpense = true)
         assertTrue(result.contains("Rp"))
     }
 
     @Test
     fun `formatWithSign should handle zero amount for expense`() {
         val result = CurrencyFormatter.formatWithSign(0.0, isExpense = true)
         assertTrue(result.startsWith("-"))
     }
 
     @Test
     fun `formatWithSign should handle zero amount for income`() {
         val result = CurrencyFormatter.formatWithSign(0.0, isExpense = false)
         assertTrue(result.startsWith("+"))
     }
 }
