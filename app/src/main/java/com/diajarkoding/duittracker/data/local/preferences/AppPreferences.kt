 package com.diajarkoding.duittracker.data.local.preferences
 
 import android.content.Context
 import androidx.datastore.core.DataStore
 import androidx.datastore.preferences.core.Preferences
 import androidx.datastore.preferences.core.booleanPreferencesKey
 import androidx.datastore.preferences.core.edit
 import androidx.datastore.preferences.core.stringPreferencesKey
 import androidx.datastore.preferences.preferencesDataStore
 import dagger.hilt.android.qualifiers.ApplicationContext
 import kotlinx.coroutines.flow.Flow
 import kotlinx.coroutines.flow.map
 import javax.inject.Inject
 import javax.inject.Singleton
 
 private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")
 
 enum class AppLanguage(val code: String, val displayName: String) {
     ENGLISH("en", "English"),
     INDONESIAN("id", "Indonesia")
 }
 
 @Singleton
 class AppPreferences @Inject constructor(
     @ApplicationContext private val context: Context
 ) {
     companion object {
         private val LANGUAGE_KEY = stringPreferencesKey("app_language")
         private val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")
         private val REMINDER_HOUR_KEY = stringPreferencesKey("reminder_hour")
         private val REMINDER_MINUTE_KEY = stringPreferencesKey("reminder_minute")
     }
 
     val language: Flow<AppLanguage> = context.appPreferencesDataStore.data.map { preferences ->
         val code = preferences[LANGUAGE_KEY] ?: AppLanguage.ENGLISH.code
         AppLanguage.entries.find { it.code == code } ?: AppLanguage.ENGLISH
     }
 
     suspend fun setLanguage(language: AppLanguage) {
         context.appPreferencesDataStore.edit { preferences ->
             preferences[LANGUAGE_KEY] = language.code
         }
     }
 
     val isReminderEnabled: Flow<Boolean> = context.appPreferencesDataStore.data.map { preferences ->
         preferences[REMINDER_ENABLED_KEY] ?: false
     }
 
     suspend fun setReminderEnabled(enabled: Boolean) {
         context.appPreferencesDataStore.edit { preferences ->
             preferences[REMINDER_ENABLED_KEY] = enabled
         }
     }
 
     val reminderHour: Flow<Int> = context.appPreferencesDataStore.data.map { preferences ->
         preferences[REMINDER_HOUR_KEY]?.toIntOrNull() ?: 20
     }
 
     val reminderMinute: Flow<Int> = context.appPreferencesDataStore.data.map { preferences ->
         preferences[REMINDER_MINUTE_KEY]?.toIntOrNull() ?: 0
     }
 
     suspend fun setReminderTime(hour: Int, minute: Int) {
         context.appPreferencesDataStore.edit { preferences ->
             preferences[REMINDER_HOUR_KEY] = hour.toString()
             preferences[REMINDER_MINUTE_KEY] = minute.toString()
         }
     }
 }
