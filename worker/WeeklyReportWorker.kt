package com.circadianx.sleepsense.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WeeklyReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // Phase 2: call backend /insights/weekly and store the report locally.
        return Result.success()
    }
}

