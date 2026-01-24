 package com.diajarkoding.duittracker.domain.model
 
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertFalse
 import org.junit.Assert.assertNull
 import org.junit.Assert.assertTrue
 import org.junit.Test
 
 class TransactionResultTest {
 
     @Test
     fun `Success should contain data`() {
         val result = TransactionResult.Success(data = "test data")
 
         assertEquals("test data", result.data)
     }
 
     @Test
     fun `Success with message should contain message`() {
         val result = TransactionResult.Success(data = "data", message = "Success message")
 
         assertEquals("Success message", result.message)
     }
 
     @Test
     fun `Success with isFromCache should indicate cache status`() {
         val result = TransactionResult.Success(data = "data", isFromCache = true)
 
         assertTrue(result.isFromCache)
     }
 
     @Test
     fun `Error should contain error message`() {
         val result = TransactionResult.Error(message = "Error occurred")
 
         assertEquals("Error occurred", result.message)
     }
 
     @Test
     fun `Error with isOffline should indicate offline status`() {
         val result = TransactionResult.Error(message = "No connection", isOffline = true)
 
         assertTrue(result.isOffline)
     }
 
     @Test
     fun `Error with exception should contain exception`() {
         val exception = RuntimeException("Test exception")
         val result = TransactionResult.Error(message = "Error", exception = exception)
 
         assertEquals(exception, result.exception)
     }
 
     @Test
     fun `Loading should be singleton`() {
         val loading1 = TransactionResult.Loading
         val loading2 = TransactionResult.Loading
 
         assertEquals(loading1, loading2)
     }
 
     @Test
     fun `map on Success should transform data`() {
         val result = TransactionResult.Success(data = 10)
 
         val mapped = result.map { it * 2 }
 
         assertTrue(mapped is TransactionResult.Success)
         assertEquals(20, (mapped as TransactionResult.Success).data)
     }
 
     @Test
     fun `map on Success should preserve message and isFromCache`() {
         val result = TransactionResult.Success(data = 10, message = "Test", isFromCache = true)
 
         val mapped = result.map { it * 2 }
 
         assertTrue(mapped is TransactionResult.Success)
         assertEquals("Test", (mapped as TransactionResult.Success).message)
         assertTrue(mapped.isFromCache)
     }
 
     @Test
     fun `map on Error should return same Error`() {
         val result: TransactionResult<Int> = TransactionResult.Error(message = "Error")
 
         val mapped = result.map { it * 2 }
 
         assertTrue(mapped is TransactionResult.Error)
         assertEquals("Error", (mapped as TransactionResult.Error).message)
     }
 
     @Test
     fun `map on Loading should return Loading`() {
         val result: TransactionResult<Int> = TransactionResult.Loading
 
         val mapped = result.map { it * 2 }
 
         assertTrue(mapped is TransactionResult.Loading)
     }
 
     @Test
     fun `onSuccess should execute action for Success`() {
         val result = TransactionResult.Success(data = "test")
         var executed = false
 
         result.onSuccess { executed = true }
 
         assertTrue(executed)
     }
 
     @Test
     fun `onSuccess should not execute action for Error`() {
         val result: TransactionResult<String> = TransactionResult.Error(message = "Error")
         var executed = false
 
         result.onSuccess { executed = true }
 
         assertFalse(executed)
     }
 
     @Test
     fun `onSuccess should not execute action for Loading`() {
         val result: TransactionResult<String> = TransactionResult.Loading
         var executed = false
 
         result.onSuccess { executed = true }
 
         assertFalse(executed)
     }
 
     @Test
     fun `onError should execute action for Error`() {
         val result: TransactionResult<String> = TransactionResult.Error(message = "Error message")
         var capturedMessage: String? = null
 
         result.onError { capturedMessage = it }
 
         assertEquals("Error message", capturedMessage)
     }
 
     @Test
     fun `onError should not execute action for Success`() {
         val result = TransactionResult.Success(data = "test")
         var executed = false
 
         result.onError { executed = true }
 
         assertFalse(executed)
     }
 
     @Test
     fun `getOrNull should return data for Success`() {
         val result = TransactionResult.Success(data = "test data")
 
         val value = result.getOrNull()
 
         assertEquals("test data", value)
     }
 
     @Test
     fun `getOrNull should return null for Error`() {
         val result: TransactionResult<String> = TransactionResult.Error(message = "Error")
 
         val value = result.getOrNull()
 
         assertNull(value)
     }
 
     @Test
     fun `getOrNull should return null for Loading`() {
         val result: TransactionResult<String> = TransactionResult.Loading
 
         val value = result.getOrNull()
 
         assertNull(value)
     }
 
     @Test
     fun `getOrDefault should return data for Success`() {
         val result = TransactionResult.Success(data = "test data")
 
         val value = result.getOrDefault("default")
 
         assertEquals("test data", value)
     }
 
     @Test
     fun `getOrDefault should return default for Error`() {
         val result: TransactionResult<String> = TransactionResult.Error(message = "Error")
 
         val value = result.getOrDefault("default")
 
         assertEquals("default", value)
     }
 
     @Test
     fun `getOrDefault should return default for Loading`() {
         val result: TransactionResult<String> = TransactionResult.Loading
 
         val value = result.getOrDefault("default")
 
         assertEquals("default", value)
     }
 }
