 package com.diajarkoding.duittracker.utils
 
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.filled.Restaurant
 import androidx.compose.material.icons.filled.DirectionsCar
 import androidx.compose.material.icons.filled.ShoppingBag
 import com.diajarkoding.duittracker.R
 import com.diajarkoding.duittracker.data.model.TransactionCategory
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertNotNull
 import org.junit.Test
 
 class CategoryUtilsTest {
 
     @Test
     fun `getIcon should return Restaurant for FOOD category`() {
         val icon = CategoryUtils.getIcon(TransactionCategory.FOOD)
         assertEquals(Icons.Default.Restaurant, icon)
     }
 
     @Test
     fun `getIcon should return DirectionsCar for TRANSPORT category`() {
         val icon = CategoryUtils.getIcon(TransactionCategory.TRANSPORT)
         assertEquals(Icons.Default.DirectionsCar, icon)
     }
 
     @Test
     fun `getIcon should return ShoppingBag for SHOPPING category`() {
         val icon = CategoryUtils.getIcon(TransactionCategory.SHOPPING)
         assertEquals(Icons.Default.ShoppingBag, icon)
     }
 
     @Test
     fun `getIcon should return icon for all categories`() {
         TransactionCategory.entries.forEach { category ->
             val icon = CategoryUtils.getIcon(category)
             assertNotNull(icon)
         }
     }
 
     @Test
     fun `getColor should return non-null color for all categories`() {
         TransactionCategory.entries.forEach { category ->
             val color = CategoryUtils.getColor(category)
             assertNotNull(color)
         }
     }
 
     @Test
     fun `getDisplayName should return Food for FOOD category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.FOOD)
         assertEquals("Food", name)
     }
 
     @Test
     fun `getDisplayName should return Transport for TRANSPORT category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.TRANSPORT)
         assertEquals("Transport", name)
     }
 
     @Test
     fun `getDisplayName should return Shopping for SHOPPING category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.SHOPPING)
         assertEquals("Shopping", name)
     }
 
     @Test
     fun `getDisplayName should return Entertainment for ENTERTAINMENT category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.ENTERTAINMENT)
         assertEquals("Entertainment", name)
     }
 
     @Test
     fun `getDisplayName should return Bills for BILLS category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.BILLS)
         assertEquals("Bills", name)
     }
 
     @Test
     fun `getDisplayName should return Health for HEALTH category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.HEALTH)
         assertEquals("Health", name)
     }
 
     @Test
     fun `getDisplayName should return Education for EDUCATION category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.EDUCATION)
         assertEquals("Education", name)
     }
 
     @Test
     fun `getDisplayName should return Social for SOCIAL category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.SOCIAL)
         assertEquals("Social", name)
     }
 
     @Test
     fun `getDisplayName should return Salary for SALARY category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.SALARY)
         assertEquals("Salary", name)
     }
 
     @Test
     fun `getDisplayName should return Investment for INVESTMENT category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.INVESTMENT)
         assertEquals("Investment", name)
     }
 
     @Test
     fun `getDisplayName should return Daily Needs for DAILY_NEEDS category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.DAILY_NEEDS)
         assertEquals("Daily Needs", name)
     }
 
     @Test
     fun `getDisplayName should return Gift for GIFT category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.GIFT)
         assertEquals("Gift", name)
     }
 
     @Test
     fun `getDisplayName should return Other for OTHER category`() {
         val name = CategoryUtils.getDisplayName(TransactionCategory.OTHER)
         assertEquals("Other", name)
     }
 
     @Test
     fun `getDisplayName should return non-empty string for all categories`() {
         TransactionCategory.entries.forEach { category ->
             val name = CategoryUtils.getDisplayName(category)
             assertNotNull(name)
             assert(name.isNotEmpty())
         }
     }
 
     @Test
     fun `getDisplayNameResId should return valid resource for FOOD category`() {
         val resId = CategoryUtils.getDisplayNameResId(TransactionCategory.FOOD)
         assertEquals(R.string.category_food, resId)
     }
 
     @Test
     fun `getDisplayNameResId should return valid resource for TRANSPORT category`() {
         val resId = CategoryUtils.getDisplayNameResId(TransactionCategory.TRANSPORT)
         assertEquals(R.string.category_transport, resId)
     }
 
     @Test
     fun `getDisplayNameResId should return non-zero for all categories`() {
         TransactionCategory.entries.forEach { category ->
             val resId = CategoryUtils.getDisplayNameResId(category)
             assert(resId != 0)
         }
     }
 }
