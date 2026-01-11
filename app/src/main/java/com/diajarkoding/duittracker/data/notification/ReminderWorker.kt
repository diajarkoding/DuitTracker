package com.diajarkoding.duittracker.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationManager: ReminderNotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Determine if this is lunch or evening reminder based on current time
        // Lunch reminder window: 11:00 - 14:00
        // Evening reminder window: 21:00 - 23:00
        val isLunchTime = currentHour in 11..14
        
        notificationManager.showReminderNotification(isLunchTime)
        return Result.success()
    }
}
