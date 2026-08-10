package com.ysompo.englishwords.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ysompo.englishwords.R
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.repo.ProgressRepository
import java.time.LocalDate

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIFICATION_ID = 1

        suspend fun shouldNotify(progressRepository: ProgressRepository, today: LocalDate): Boolean {
            val completion = progressRepository.completionForDate(today)
            return !StreakCalculator.isDayComplete(completion)
        }
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val progressRepository = ProgressRepository(db)

        if (shouldNotify(progressRepository, LocalDate.now())) {
            postNotification()
        }
        return Result.success()
    }

    private fun postNotification() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, applicationContext.getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText(applicationContext.getString(R.string.reminder_notification_text))
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}
