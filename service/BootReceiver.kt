package com.circadianx.sleepsense.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Phase 1: re-schedule alarms / workers on reboot.
        // Keep minimal for now; exact behavior added during routines + reminders.
    }
}

