 package com.diajarkoding.duittracker.data.notification
 
 import android.Manifest
 import android.app.NotificationChannel
 import android.app.NotificationManager
 import android.app.PendingIntent
 import android.content.Context
 import android.content.Intent
 import android.content.pm.PackageManager
 import android.os.Build
 import androidx.core.app.NotificationCompat
 import androidx.core.app.NotificationManagerCompat
 import androidx.core.content.ContextCompat
 import androidx.work.ExistingPeriodicWorkPolicy
 import androidx.work.PeriodicWorkRequestBuilder
 import androidx.work.WorkManager
 import com.diajarkoding.duittracker.MainActivity
 import com.diajarkoding.duittracker.R
 import dagger.hilt.android.qualifiers.ApplicationContext
 import java.util.Calendar
 import java.util.concurrent.TimeUnit
 import javax.inject.Inject
 import javax.inject.Singleton
 
 @Singleton
 class ReminderNotificationManager @Inject constructor(
     @ApplicationContext private val context: Context
 ) {
     companion object {
         const val CHANNEL_ID = "daily_reminder_channel"
         const val NOTIFICATION_ID = 1001
         const val REMINDER_WORK_NAME = "daily_reminder_work"
     }
 
     fun createNotificationChannel() {
         val name = context.getString(R.string.notification_channel_name)
         val descriptionText = context.getString(R.string.notification_channel_description)
         val importance = NotificationManager.IMPORTANCE_DEFAULT
         val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
             description = descriptionText
         }
         val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
         notificationManager.createNotificationChannel(channel)
     }
 
     fun showReminderNotification() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (ContextCompat.checkSelfPermission(
                     context,
                     Manifest.permission.POST_NOTIFICATIONS
                 ) != PackageManager.PERMISSION_GRANTED
             ) {
                 return
             }
         }
 
         val intent = Intent(context, MainActivity::class.java).apply {
             flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
         }
         val pendingIntent = PendingIntent.getActivity(
             context,
             0,
             intent,
             PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
         )
 
         val builder = NotificationCompat.Builder(context, CHANNEL_ID)
             .setSmallIcon(R.drawable.ic_notification)
             .setContentTitle(context.getString(R.string.notification_title))
             .setContentText(context.getString(R.string.notification_message))
             .setPriority(NotificationCompat.PRIORITY_DEFAULT)
             .setContentIntent(pendingIntent)
             .setAutoCancel(true)
 
         NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
     }
 
     fun scheduleReminder(hour: Int, minute: Int) {
         val currentTime = Calendar.getInstance()
         val targetTime = Calendar.getInstance().apply {
             set(Calendar.HOUR_OF_DAY, hour)
             set(Calendar.MINUTE, minute)
             set(Calendar.SECOND, 0)
             set(Calendar.MILLISECOND, 0)
         }
 
         if (targetTime.before(currentTime)) {
             targetTime.add(Calendar.DAY_OF_YEAR, 1)
         }
 
         val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
 
         val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
             24, TimeUnit.HOURS
         )
             .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
             .build()
 
         WorkManager.getInstance(context)
             .enqueueUniquePeriodicWork(
                 REMINDER_WORK_NAME,
                 ExistingPeriodicWorkPolicy.UPDATE,
                 reminderRequest
             )
     }
 
     fun cancelReminder() {
         WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
     }
 
     fun hasNotificationPermission(): Boolean {
         return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             ContextCompat.checkSelfPermission(
                 context,
                 Manifest.permission.POST_NOTIFICATIONS
             ) == PackageManager.PERMISSION_GRANTED
         } else {
             true
         }
     }
 }
