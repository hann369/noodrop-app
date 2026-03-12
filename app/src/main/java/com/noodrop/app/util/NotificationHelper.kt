package com.noodrop.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.noodrop.app.MainActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val CHANNEL_DAILY   = "noodrop_daily"
const val CHANNEL_PREMIUM = "noodrop_premium"
const val PREFS_NOTIF     = "noodrop_notif_prefs"
const val KEY_NOTIF_HOUR  = "notif_hour"
const val KEY_NOTIF_MIN   = "notif_minute"
const val KEY_NOTIF_ON    = "notif_enabled"

fun createNotificationChannels(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_DAILY, "Daily Reminder", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Daily stack reminder"
        }
    )
    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_PREMIUM, "Premium Hints", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Premium upgrade hints"
        }
    )
}

fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
    val now    = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
    }
    val delay = target.timeInMillis - now.timeInMillis

    val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("hour" to hour, "minute" to minute))
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_reminder",
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
    )

    context.getSharedPreferences(PREFS_NOTIF, Context.MODE_PRIVATE).edit()
        .putInt(KEY_NOTIF_HOUR, hour)
        .putInt(KEY_NOTIF_MIN, minute)
        .putBoolean(KEY_NOTIF_ON, true)
        .apply()
}

fun cancelDailyReminder(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
    context.getSharedPreferences(PREFS_NOTIF, Context.MODE_PRIVATE).edit()
        .putBoolean(KEY_NOTIF_ON, false)
        .apply()
}

fun schedulePremiumHint(context: Context) {
    WorkManager.getInstance(context).enqueueUniqueWork(
        "premium_hint",
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<PremiumHintWorker>()
            .setInitialDelay(3, TimeUnit.DAYS)
            .build()
    )
}

class DailyReminderWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        val messages = listOf(
            "Time to take your stack \uD83E\uDDEC",
            "Don't break your streak! Check in today \uD83D\uDD25",
            "Your stack is waiting \u26A1",
            "Stay consistent — log your stack today \uD83C\uDFAF",
        )
        nm.notify(
            1001,
            NotificationCompat.Builder(context, CHANNEL_DAILY)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Noodrop")
                .setContentText(messages.random())
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
        )
        return Result.success()
    }
}

class PremiumHintWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pi = PendingIntent.getActivity(
            context, 1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(
            1002,
            NotificationCompat.Builder(context, CHANNEL_PREMIUM)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Unlock Premium Protocols \u2756")
                .setContentText("Focus & Longevity protocols are waiting — upgrade for \u20AC9,99/mo")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
        )
        return Result.success()
    }
}
