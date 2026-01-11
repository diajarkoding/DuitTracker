package com.diajarkoding.duittracker.data.notification

import android.Manifest
import android.app.AlarmManager
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
        const val NOTIFICATION_ID_LUNCH = 1001
        const val NOTIFICATION_ID_EVENING = 1002
        const val REMINDER_WORK_LUNCH = "daily_reminder_lunch"
        const val REMINDER_WORK_EVENING = "daily_reminder_evening"
        
        // Fixed reminder times
        const val LUNCH_HOUR = 12
        const val LUNCH_MINUTE = 0
        const val EVENING_HOUR = 22
        const val EVENING_MINUTE = 0
    }

    fun createNotificationChannel() {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification(isLunchTime: Boolean = true) {
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
            putExtra("open_add_transaction", true)
        }
        
        val notificationId = if (isLunchTime) NOTIFICATION_ID_LUNCH else NOTIFICATION_ID_EVENING
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val message = if (isLunchTime) {
            context.getString(R.string.notification_message_lunch)
        } else {
            context.getString(R.string.notification_message_evening)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun scheduleDualReminders() {
        scheduleLunchReminder()
        scheduleEveningReminder()
    }

    private fun scheduleLunchReminder() {
        scheduleReminder(LUNCH_HOUR, LUNCH_MINUTE, REMINDER_WORK_LUNCH)
    }

    private fun scheduleEveningReminder() {
        scheduleReminder(EVENING_HOUR, EVENING_MINUTE, REMINDER_WORK_EVENING)
    }

    private fun scheduleReminder(hour: Int, minute: Int, workName: String) {
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
            .addTag(workName)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderRequest
            )
    }

    @Deprecated("Use scheduleDualReminders instead")
    fun scheduleReminder(hour: Int, minute: Int) {
        scheduleDualReminders()
    }

    fun cancelReminder() {
        cancelAllReminders()
    }

    fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_LUNCH)
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_EVENING)
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

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
