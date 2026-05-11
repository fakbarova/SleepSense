package com.circadianx.sleepsense.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.circadianx.sleepsense.R
import com.circadianx.sleepsense.data.local.db.dao.NightDisturbanceDao
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.entity.NightDisturbanceEntity
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class SleepTrackingService : Service() {
    @Inject lateinit var prefs: UserPreferencesRepository
    @Inject lateinit var sleepRecordDao: SleepRecordDao
    @Inject lateinit var nightDisturbanceDao: NightDisturbanceDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var activeSleepStartMs: Long? = null
    private var lastScreenOnMs: Long? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> onScreenOn()
                Intent.ACTION_SCREEN_OFF -> onScreenOff()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
        )
    }

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                TRACKING_CHANNEL_ID,
                "Sleep tracking",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        return NotificationCompat.Builder(this, TRACKING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SleepSense is tracking sleep")
            .setContentText("Detecting bedtime, wake time, and disturbances.")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun onScreenOn() {
        val now = System.currentTimeMillis()
        lastScreenOnMs = now

        scope.launch {
            val schedule = prefs.bedtimeSchedule.first() ?: return@launch
            if (isInSleepWindow(now, schedule.targetBedtimeMinutes, schedule.targetWakeMinutes)) {
                // We only start a sleep session once the phone goes idle after bedtime; screen-on events are disturbances.
                if (activeSleepStartMs != null) {
                    // disturbance duration will be measured on screen off
                }
            } else {
                // Outside sleep window: if we have an active session and we're near wake time, consider ending it.
                if (activeSleepStartMs != null && isInWakeEndWindow(now, schedule.targetWakeMinutes)) {
                    endSleepSession(now)
                }
            }
        }
    }

    private fun onScreenOff() {
        val now = System.currentTimeMillis()

        scope.launch {
            val schedule = prefs.bedtimeSchedule.first() ?: return@launch

            // If the screen just turned off and we're around bedtime, treat it as \"user went to bed\" if no active session.
            if (activeSleepStartMs == null && isInBedStartWindow(now, schedule.targetBedtimeMinutes)) {
                activeSleepStartMs = now
            }

            // If we had a screen-on moment during an active sleep session, log it as a disturbance.
            val screenOnAt = lastScreenOnMs
            if (activeSleepStartMs != null && screenOnAt != null) {
                val duration = (now - screenOnAt).coerceAtLeast(0)
                if (duration >= MIN_DISTURBANCE_MS) {
                    nightDisturbanceDao.insert(
                        NightDisturbanceEntity(
                            timestampMs = screenOnAt,
                            durationMs = duration
                        )
                    )
                }
            }

            lastScreenOnMs = null
        }
    }

    private suspend fun endSleepSession(endMs: Long) {
        val startMs = activeSleepStartMs ?: return
        val disturbanceCount = nightDisturbanceDao.countInRange(startMs, endMs)
        val durationMinutes = ((endMs - startMs) / 60_000L).toInt().coerceAtLeast(0)

        // Placeholder score; Phase 1 routines will replace with full scoring.
        val score = (durationMinutes / 6.0 * 100).toInt().coerceIn(0, 100) - disturbanceCount * 4
        val normalizedScore = score.coerceIn(0, 100)

        sleepRecordDao.insert(
            SleepRecordEntity(
                startTimeMs = startMs,
                endTimeMs = endMs,
                disturbanceCount = disturbanceCount,
                sleepScore = normalizedScore
            )
        )

        postMorningSummaryNotification(normalizedScore, disturbanceCount, durationMinutes)

        activeSleepStartMs = null
        lastScreenOnMs = null
    }

    private fun postMorningSummaryNotification(score: Int, disturbances: Int, durationMinutes: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        val durationText = "${hours}h ${mins}m"

        val notification = NotificationCompat.Builder(this, MORNING_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Morning summary")
            .setContentText("Sleep score $score/100 · $disturbances disturbances · $durationText")
            .setAutoCancel(true)
            .build()

        nm.notify(MORNING_SUMMARY_NOTIFICATION_ID, notification)
    }

    private fun isInBedStartWindow(nowMs: Long, targetBedtimeMinutes: Int): Boolean {
        val now = minutesOfDay(nowMs)
        val start = (targetBedtimeMinutes - 60).floorMod(24 * 60)
        val end = (targetBedtimeMinutes + 60).floorMod(24 * 60)
        return isMinutesInWrapRange(now, start, end)
    }

    private fun isInSleepWindow(nowMs: Long, targetBedtimeMinutes: Int, targetWakeMinutes: Int): Boolean {
        val now = minutesOfDay(nowMs)
        return isMinutesInWrapRange(now, targetBedtimeMinutes.floorMod(24 * 60), targetWakeMinutes.floorMod(24 * 60))
    }

    private fun isInWakeEndWindow(nowMs: Long, targetWakeMinutes: Int): Boolean {
        val now = minutesOfDay(nowMs)
        val start = (targetWakeMinutes - 120).floorMod(24 * 60)
        val end = (targetWakeMinutes + 120).floorMod(24 * 60)
        return isMinutesInWrapRange(now, start, end)
    }

    private fun minutesOfDay(epochMs: Long): Int {
        val lt = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalTime()
        return lt.hour * 60 + lt.minute
    }

    private fun isMinutesInWrapRange(value: Int, start: Int, end: Int): Boolean {
        return if (start <= end) value in start..end else value >= start || value <= end
    }

    private fun Int.floorMod(mod: Int): Int = ((this % mod) + mod) % mod

    private companion object {
        const val NOTIFICATION_ID = 7201
        const val TRACKING_CHANNEL_ID = "sleep_tracking"
        const val MIN_DISTURBANCE_MS = 10_000L
        const val MORNING_SUMMARY_CHANNEL_ID = "sleep_summary"
        const val MORNING_SUMMARY_NOTIFICATION_ID = 7202
    }
}

