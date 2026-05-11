package com.circadianx.sleepsense.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.circadianx.sleepsense.R
import com.circadianx.sleepsense.data.local.db.dao.StepDao
import com.circadianx.sleepsense.data.local.db.entity.StepDayEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {
    @Inject lateinit var stepDao: StepDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    private var baselineSteps: Float? = null
    private var lastEpochDay: Long = LocalDate.now().toEpochDay()

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        stepSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        val value = event.values.firstOrNull() ?: return
        val epochDay = LocalDate.now().toEpochDay()

        if (epochDay != lastEpochDay) {
            // New day: reset baseline using current sensor value.
            lastEpochDay = epochDay
            baselineSteps = value
        }

        val base = baselineSteps ?: run {
            baselineSteps = value
            value
        }

        val stepsToday = (value - base).toInt().coerceAtLeast(0)
        scope.launch {
            stepDao.upsert(
                StepDayEntity(
                    epochDay = epochDay,
                    steps = stepsToday,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Step tracking",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SleepSense is tracking steps")
            .setContentText("Counting steps in the background.")
            .setOngoing(true)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "step_tracking"
        const val NOTIFICATION_ID = 7301
    }
}

