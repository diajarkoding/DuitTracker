 package com.diajarkoding.duittracker.data.local.security
 
 import android.content.Context
 import android.content.SharedPreferences
 import androidx.security.crypto.EncryptedSharedPreferences
 import androidx.security.crypto.MasterKey
 import dagger.hilt.android.qualifiers.ApplicationContext
 import javax.inject.Inject
 import javax.inject.Singleton
 
 /**
  * Provides encrypted storage for sensitive user data.
  * 
  * Uses Android Keystore-backed encryption via EncryptedSharedPreferences
  * to securely store sensitive information such as:
  * - User session tokens
  * - Cached credentials
  * - Security-sensitive preferences
  * 
  * All data is encrypted using AES-256 GCM encryption.
  */
 @Singleton
 class SecurePreferences @Inject constructor(
     @ApplicationContext private val context: Context
 ) {
     companion object {
         private const val PREFS_NAME = "duittracker_secure_prefs"
         private const val KEY_USER_SESSION = "user_session_token"
         private const val KEY_LAST_SYNC_TOKEN = "last_sync_token"
         private const val KEY_DEVICE_ID = "device_id"
     }
     
     private val masterKey: MasterKey by lazy {
         MasterKey.Builder(context)
             .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
             .build()
     }
     
     private val encryptedPrefs: SharedPreferences by lazy {
         EncryptedSharedPreferences.create(
             context,
             PREFS_NAME,
             masterKey,
             EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
             EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
         )
     }
     
     /**
      * Stores user session token securely.
      * @param token The session token to store
      */
     fun setUserSession(token: String?) {
         encryptedPrefs.edit().apply {
             if (token != null) {
                 putString(KEY_USER_SESSION, token)
             } else {
                 remove(KEY_USER_SESSION)
             }
         }.apply()
     }
     
     /**
      * Retrieves stored user session token.
      * @return The stored session token or null if not set
      */
     fun getUserSession(): String? {
         return encryptedPrefs.getString(KEY_USER_SESSION, null)
     }
     
     /**
      * Stores last sync token for incremental sync operations.
      * @param token The sync token to store
      */
     fun setLastSyncToken(token: String?) {
         encryptedPrefs.edit().putString(KEY_LAST_SYNC_TOKEN, token).apply()
     }
     
     /**
      * Retrieves the last sync token.
      * @return The stored sync token or null if not set
      */
     fun getLastSyncToken(): String? {
         return encryptedPrefs.getString(KEY_LAST_SYNC_TOKEN, null)
     }
     
     /**
      * Gets or creates a unique device identifier.
      * This ID persists across app reinstalls (until app data is cleared).
      * @return Unique device identifier string
      */
     fun getOrCreateDeviceId(): String {
         val existingId = encryptedPrefs.getString(KEY_DEVICE_ID, null)
         if (existingId != null) return existingId
         
         val newId = java.util.UUID.randomUUID().toString()
         encryptedPrefs.edit().putString(KEY_DEVICE_ID, newId).apply()
         return newId
     }
     
     /**
      * Clears all secure preferences.
      * Should be called on user logout.
      */
     fun clearAll() {
         encryptedPrefs.edit().clear().apply()
     }
 }
