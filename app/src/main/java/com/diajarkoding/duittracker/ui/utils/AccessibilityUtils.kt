 package com.diajarkoding.duittracker.ui.utils
 
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.semantics.Role
 import androidx.compose.ui.semantics.contentDescription
 import androidx.compose.ui.semantics.role
 import androidx.compose.ui.semantics.semantics
 import androidx.compose.ui.semantics.stateDescription
 
 /**
  * Accessibility utility functions for improving screen reader support.
  * 
  * This object provides extension functions and utilities to ensure
  * the app is accessible to users with disabilities, following WCAG guidelines.
  */
 object AccessibilityUtils {
     
     /**
      * Minimum touch target size in dp as per Material Design guidelines.
      * Touch targets should be at least 48dp x 48dp for accessibility.
      */
     const val MIN_TOUCH_TARGET_SIZE = 48
     
     /**
      * Creates a content description for currency values.
      * 
      * @param amount The monetary amount
      * @param isExpense Whether this is an expense (negative) or income (positive)
      * @return Accessible description for screen readers
      */
     fun formatCurrencyForAccessibility(amount: Double, isExpense: Boolean): String {
         val formattedAmount = String.format("%.0f", amount)
         val type = if (isExpense) "expense" else "income"
         return "$type of $formattedAmount rupiah"
     }
     
     /**
      * Creates a content description for transaction items.
      * 
      * @param category The transaction category
      * @param amount The transaction amount
      * @param note The transaction note
      * @param isExpense Whether this is an expense
      * @return Full accessible description for the transaction
      */
     fun formatTransactionForAccessibility(
         category: String,
         amount: Double,
         note: String,
         isExpense: Boolean
     ): String {
         val type = if (isExpense) "Expense" else "Income"
         val formattedAmount = String.format("%.0f", amount)
         return "$type: $category, $formattedAmount rupiah, $note"
     }
 }
 
 /**
  * Extension function to add accessibility description to a Modifier.
  * 
  * @param description The content description for screen readers
  * @return Modified Modifier with accessibility info
  */
 fun Modifier.accessibilityDescription(description: String): Modifier {
     return this.semantics {
         contentDescription = description
     }
 }
 
 /**
  * Extension function to mark an element as a button for accessibility.
  * 
  * @param description The content description
  * @return Modified Modifier with button role
  */
 fun Modifier.accessibilityButton(description: String): Modifier {
     return this.semantics {
         contentDescription = description
         role = Role.Button
     }
 }
 
 /**
  * Extension function to add state description for toggle elements.
  * 
  * @param isEnabled Whether the element is enabled/active
  * @param enabledText Description when enabled
  * @param disabledText Description when disabled
  * @return Modified Modifier with state description
  */
 fun Modifier.accessibilityToggleState(
     isEnabled: Boolean,
     enabledText: String = "Enabled",
     disabledText: String = "Disabled"
 ): Modifier {
     return this.semantics {
         stateDescription = if (isEnabled) enabledText else disabledText
     }
 }
