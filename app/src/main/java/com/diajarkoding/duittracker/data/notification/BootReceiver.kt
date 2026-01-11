 package com.diajarkoding.duittracker.data.notification
 
 import android.content.BroadcastReceiver
 import android.content.Context
 import android.content.Intent
 import androidx.work.Configuration
 import androidx.work.WorkManager
 import com.diajarkoding.duittracker.data.local.preferences.AppPreferences
 import dagger.hilt.android.AndroidEntryPoint
 import kotlinx.coroutines.CoroutineScope
 import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.flow.first
 import kotlinx.coroutines.launch
 import javax.inject.Inject
 
 @AndroidEntryPoint
 class BootReceiver : BroadcastReceiver() {
 
     @Inject
     lateinit var appPreferences: AppPreferences
 
     @Inject
     lateinit var reminderNotificationManager: ReminderNotificationManager
 
     override fun onReceive(context: Context, intent: Intent) {
         if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
             intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
             intent.action == "android.intent.action.QUICKBOOT_POWERON") {
             
             val pendingResult = goAsync()
             
             CoroutineScope(Dispatchers.IO).launch {
                 try {
                     val isReminderEnabled = appPreferences.isReminderEnabled.first()
                     if (isReminderEnabled) {
                         reminderNotificationManager.scheduleDualReminders()
                     }
                 } finally {
                     pendingResult.finish()
                 }
             }
         }
     }
 }
