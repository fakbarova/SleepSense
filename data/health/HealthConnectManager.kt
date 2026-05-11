package com.circadianx.sleepsense.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

class HealthConnectManager(private val context: Context) {
    fun isAvailable(): Boolean = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
}

