package com.circadianx.sleepsense.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.circadianx.sleepsense.R

class RoutineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND) return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Routine reminder"

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Tap to open SleepSense and check it off.")
            .setAutoCancel(true)
            .build()

        nm.notify((intent.getLongExtra(EXTRA_ITEM_ID, 0L) % Int.MAX_VALUE).toInt(), notification)
    }

    companion object {
        const val ACTION_REMIND = "com.circadianx.sleepsense.ACTION_ROUTINE_REMIND"
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_TITLE = "title"
        const val CHANNEL_ID = "sleep_summary"
    }
}

