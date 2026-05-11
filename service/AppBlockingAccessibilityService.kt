package com.circadianx.sleepsense.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
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
class AppBlockingAccessibilityService : AccessibilityService() {
    @Inject lateinit var prefs: UserPreferencesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val blockedPackages = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.facebook.katana",
        "com.twitter.android",
        "com.google.android.youtube",
        "com.netflix.mediaclient"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in blockedPackages) return
        if (pkg == packageName) return

        scope.launch {
            val schedule = prefs.bedtimeSchedule.first() ?: return@launch
            val nowMinutes = minutesOfDay(System.currentTimeMillis())

            val bedtime = schedule.targetBedtimeMinutes
            val windDownStart = (bedtime - 30).floorMod(24 * 60)
            val windDownEnd = (bedtime + 120).floorMod(24 * 60)

            if (!isMinutesInWrapRange(nowMinutes, windDownStart, windDownEnd)) return@launch

            startActivity(
                Intent(this@AppBlockingAccessibilityService, BlockedAppActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(BlockedAppActivity.EXTRA_PACKAGE, pkg)
                }
            )
        }
    }

    override fun onInterrupt() = Unit

    private fun minutesOfDay(epochMs: Long): Int {
        val lt = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalTime()
        return lt.hour * 60 + lt.minute
    }

    private fun isMinutesInWrapRange(value: Int, start: Int, end: Int): Boolean {
        return if (start <= end) value in start..end else value >= start || value <= end
    }

    private fun Int.floorMod(mod: Int): Int = ((this % mod) + mod) % mod
}

