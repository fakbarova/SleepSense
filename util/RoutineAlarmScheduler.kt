package com.circadianx.sleepsense.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.circadianx.sleepsense.service.RoutineReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object RoutineAlarmScheduler {
    private const val TAG = "RoutineAlarmScheduler"

    fun scheduleReminder(
        context: Context,
        itemId: Long,
        title: String,
        minutesOfDay: Int
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = LocalDateTime.now()
        val triggerToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(minutesOfDay / 60, minutesOfDay % 60))
        val trigger = if (triggerToday.isAfter(now)) triggerToday else triggerToday.plusDays(1)
        val triggerMs = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val pi = PendingIntent.getBroadcast(
            context,
            itemId.toInt(),
            Intent(context, RoutineReminderReceiver::class.java).apply {
                action = RoutineReminderReceiver.ACTION_REMIND
                putExtra(RoutineReminderReceiver.EXTRA_ITEM_ID, itemId)
                putExtra(RoutineReminderReceiver.EXTRA_TITLE, title)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarms only when the OS grants permission (Android 12+ gates this).
        // Falling back to inexact avoids SecurityException, which previously crashed the app.
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { am.canScheduleExactAlarms() }.getOrDefault(false)
        } else true

        try {
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "scheduleReminder: SecurityException, dropping alarm for $title", se)
        } catch (t: Throwable) {
            Log.w(TAG, "scheduleReminder: failed to schedule alarm for $title", t)
        }
    }

    fun cancelReminder(context: Context, itemId: Long) {
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                context,
                itemId.toInt(),
                Intent(context, RoutineReminderReceiver::class.java).apply { action = RoutineReminderReceiver.ACTION_REMIND },
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: return

            am.cancel(pi)
        } catch (t: Throwable) {
            Log.w(TAG, "cancelReminder: failed", t)
        }
    }
}

